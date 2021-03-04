package io.julian.zookeeper.write;

import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class WriteHandlerTest extends AbstractServerBase {
    private final static JsonObject MESSAGE_JSON = new JsonObject().put("test", "message");
    private final static String MESSAGE_ID = "id";
    private final static ClientMessage POST_MESSAGE = new ClientMessage(HTTPRequest.POST, MESSAGE_JSON, MESSAGE_ID);
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
        handler.initialProposalUpdate(POST_MESSAGE)
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
        handler.initialProposalUpdate(POST_MESSAGE)
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

    @Test
    public void TestAcknowledgeLeaderWhenAck(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry());

        Async async = context.async();
        handler.acknowledgeLeader(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), POST_MESSAGE.toJson(), new ShortenedExchange(MessagePhase.ACK, ID).toJson()))
            .onComplete(context.asyncAssertSuccess(v -> {
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getState().getMessageStore().getNumberOfMessages());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestAcknowledgeLeaderWhenCommit(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry());
        handler.getState().addProposal(new Proposal(POST_MESSAGE, ID));
        Async async = context.async();
        handler.acknowledgeLeader(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), POST_MESSAGE.toJson(), new ShortenedExchange(MessagePhase.COMMIT, ID).toJson()))
            .onComplete(context.asyncAssertSuccess(v -> {
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(1, handler.getState().getMessageStore().getNumberOfMessages());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    private CandidateInformationRegistry createTestCandidateInformationRegistry() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT, 1));
        registry.updateNextLeader();
        return registry;
    }

    private WriteHandler createWriteHandler(final CandidateInformationRegistry candidateInformationRegistry) {
        return new WriteHandler(new Controller(new Configuration()), new MessageStore(), candidateInformationRegistry, createServerClient(), createTestRegistryManager(), vertx);
    }

    private WriteHandler createWriteHandler() {
        return new WriteHandler(new Controller(new Configuration()), new MessageStore(), createTestCandidateInformationRegistry(), createServerClient(), createTestRegistryManager(), vertx);
    }
}
