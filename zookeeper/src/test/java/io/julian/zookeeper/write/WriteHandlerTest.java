package io.julian.zookeeper.write;

import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class WriteHandlerTest extends AbstractServerBase {
    private final static JsonObject MESSAGE_JSON = new JsonObject().put("test", "message");
    private final static HTTPRequest REQUEST = HTTPRequest.POST;
    private final static String MESSAGE_ID = "id";
    private final static ClientMessage MESSAGE = new ClientMessage(REQUEST, MESSAGE_JSON, MESSAGE_ID);
    private final static Zxid ID = new Zxid(0, 0);

    @Test
    public void TestInitialization() {
        WriteHandler write = createWriteHandler();
        Assert.assertEquals(0, write.getCounter());
        Assert.assertEquals(0, write.getLeaderEpoch());
        Assert.assertEquals(0, write.getState().getHistory().size());
        Assert.assertNotNull(write.getProposalTracker());
    }

    @Test
    public void TestInitialProposalUpdate(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler();

        Async async = context.async();
        handler.initialProposalUpdate(MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(1, handler.getState().getHistory().size());
                Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());
                Assert.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                Assert.assertEquals(0, handler.getLeaderEpoch());
                Assert.assertEquals(1, handler.getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestInitialProposalUpdateFails(final TestContext context) {
        WriteHandler handler = createWriteHandler();

        Async async = context.async();
        handler.initialProposalUpdate(MESSAGE)
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(1, handler.getState().getHistory().size());
                Assert.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                Assert.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                Assert.assertEquals(0, handler.getLeaderEpoch());
                Assert.assertEquals(1, handler.getCounter());
                async.complete();
            }));

        async.awaitSuccess();
    }

    @Test
    public void TestAddAcknowledgementAndAttemptToBroadcastCommitDoesNotBroadcastWhenNotMajority(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpApiServer(context, SECOND_SERVER_CONFIG);

        CandidateInformationRegistry registry = createTestCandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(SECOND_SERVER_CONFIG.getHost(), SECOND_SERVER_CONFIG.getPort(), 1234));

        WriteHandler handler = createWriteHandler(registry);
        handler.getProposalTracker().addAcknowledgedProposalTracker(ID);
        Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());

        Async async = context.async();
        handler.addAcknowledgementAndAttemptToBroadcastCommit(ID)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(0, handler.getState().getHistory().size());
                Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());
                Assert.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                Assert.assertEquals(0, handler.getLeaderEpoch());
                Assert.assertEquals(0, handler.getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
        tearDownServer(context, server2);
    }

    @Test
    public void TestAddAcknowledgementAndAttemptToBroadcastCommitDoesNotBroadcastWhenMajority(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, DEFAULT_SEVER_CONFIG);

        WriteHandler handler = createWriteHandler();
        handler.getProposalTracker().addAcknowledgedProposalTracker(ID);
        Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());

        Async async = context.async();
        handler.addAcknowledgementAndAttemptToBroadcastCommit(ID)
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(0, handler.getState().getHistory().size());
                Assert.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                Assert.assertEquals(1, handler.getProposalTracker().getCommittedProposals().size());
                Assert.assertEquals(0, handler.getLeaderEpoch());
                Assert.assertEquals(0, handler.getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    private CandidateInformationRegistry createTestCandidateInformationRegistry() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT, 1));
        return registry;
    }

    private WriteHandler createWriteHandler(final CandidateInformationRegistry candidateInformationRegistry) {
        return new WriteHandler(new Controller(new Configuration()), candidateInformationRegistry, createServerClient(), createTestRegistryManager(), vertx);
    }

    private WriteHandler createWriteHandler() {
        return new WriteHandler(new Controller(new Configuration()), createTestCandidateInformationRegistry(), createServerClient(), createTestRegistryManager(), vertx);
    }
}
