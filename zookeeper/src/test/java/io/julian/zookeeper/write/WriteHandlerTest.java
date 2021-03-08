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
import io.julian.zookeeper.election.LeadershipElectionHandler;
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
        Assert.assertEquals(0, write.getState().getCounter());
        Assert.assertEquals(0, write.getState().getLeaderEpoch());
        Assert.assertEquals(0, write.getState().getHistory().size());
        Assert.assertNotNull(write.getProposalTracker());
    }

    @Test
    public void TestInitialProposalUpdate(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler();

        Async async = context.async();
        handler.initialProposalUpdate(POST_MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(1, handler.getState().getMessageStore().getNumberOfMessages());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(1, handler.getState().getCounter());
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
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(1, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
    }

    @Test
    public void TestAddAcknowledgementAndAttemptToBroadcastCommitDoesNotBroadcastWhenNotMajority(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpBasicApiServer(context, SECOND_SERVER_CONFIG);

        CandidateInformationRegistry registry = createTestCandidateInformationRegistry(false);
        registry.addCandidateInformation(new CandidateInformation(SECOND_SERVER_CONFIG.getHost(), SECOND_SERVER_CONFIG.getPort(), 1234));

        WriteHandler handler = createWriteHandler(registry);
        handler.getProposalTracker().addAcknowledgedProposalTracker(ID);
        Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());

        Async async = context.async();
        handler.addAcknowledgementAndAttemptToBroadcastCommit(ID)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(0, handler.getState().getHistory().size());
                context.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(0, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
        tearDownServer(context, server2);
    }

    @Test
    public void TestAddAcknowledgementAndAttemptToBroadcastCommitDoesNotBroadcastWhenMajority(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);

        WriteHandler handler = createWriteHandler();
        handler.getProposalTracker().addAcknowledgedProposalTracker(ID);
        Assert.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());

        Async async = context.async();
        handler.addAcknowledgementAndAttemptToBroadcastCommit(ID)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(0, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(1, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(0, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestAcknowledgeLeaderWhenAck(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(true));

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
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(false));
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

    @Test
    public void TestHandleCoordinationMessageAsLeaderBroadcastsUpdate(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(false));
        handler.getController().setLabel(LeadershipElectionHandler.LEADER_LABEL);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST), POST_MESSAGE.toJson(), null))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(1, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(1, handler.getState().getMessageStore().getNumberOfMessages());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(1, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsLeaderBroadcastsCommit(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(false));
        handler.getController().setLabel(LeadershipElectionHandler.LEADER_LABEL);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), POST_MESSAGE.toJson(), new ShortenedExchange(MessagePhase.COMMIT, new Zxid(0, 0)).toJson()))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(0, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getMessageStore().getNumberOfMessages());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(0, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsFollowerBroadcastsCommit(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(true));
        handler.getController().setLabel(LeadershipElectionHandler.FOLLOWER_LABEL);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), POST_MESSAGE.toJson(), new ShortenedExchange(MessagePhase.ACK, new Zxid(0, 0)).toJson()))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(1, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getMessageStore().getNumberOfMessages());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(0, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleClientMessageAsFollowerForwardsToLeader(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        WriteHandler handler = createWriteHandler(createTestCandidateInformationRegistry(true));
        handler.getController().setLabel(LeadershipElectionHandler.FOLLOWER_LABEL);
        Async async = context.async();
        handler.handleClientMessage(POST_MESSAGE)
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(0, handler.getState().getHistory().size());
                context.assertEquals(0, handler.getProposalTracker().getAcknowledgedProposals().size());
                context.assertEquals(0, handler.getProposalTracker().getCommittedProposals().size());
                context.assertEquals(0, handler.getState().getMessageStore().getNumberOfMessages());
                context.assertEquals(0, handler.getState().getLeaderEpoch());
                context.assertEquals(0, handler.getState().getCounter());
                async.complete();
            }));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    private WriteHandler createWriteHandler(final CandidateInformationRegistry candidateInformationRegistry) {
        return new WriteHandler(new Controller(new Configuration()), new MessageStore(), candidateInformationRegistry, createServerClient(), createTestRegistryManager(), vertx);
    }

    private WriteHandler createWriteHandler() {
        return new WriteHandler(new Controller(new Configuration()), new MessageStore(), createTestCandidateInformationRegistry(false), createServerClient(), createTestRegistryManager(), vertx);
    }
}
