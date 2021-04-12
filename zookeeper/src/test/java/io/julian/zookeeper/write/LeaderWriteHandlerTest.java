package io.julian.zookeeper.write;

import io.julian.TestMetricsCollector;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.julian.zookeeper.models.ZxidTest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LeaderWriteHandlerTest extends AbstractServerBase {
    private final static int MAJORITY = 2;
    private final static String MESSAGE_ID = "id-1234";
    private final static ClientMessage MESSAGE = new ClientMessage(HTTPRequest.POST,
        new JsonObject().put("random", "key"), MESSAGE_ID);
    private final static ClientMessage NULL_ID_MESSAGE = new ClientMessage(HTTPRequest.POST,
        new JsonObject().put("random", "key"), null);
    private final static Zxid ID = new Zxid(ZxidTest.EPOCH, ZxidTest.COUNTER);

    @Test
    public void TestBroadcastInitialProposalIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);

        Async async = context.async();
        handler.broadcastInitialProposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), "id"), new Zxid(0, 1))
            .onComplete(context.asyncAssertSuccess(res -> vertx.setTimer(500, v -> async.complete())));

        async.awaitSuccess();
        collector.testHasExpectedStatusSize(1);
        tearDownServer(context, server);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestBroadcastInitialProposalFails(final TestContext context) {
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);
        Async async = context.async();
        handler.broadcastInitialProposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), "id"), new Zxid(0, 1))
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                context.assertEquals(1, handler.getDeadCoordinationMessages().size());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestBroadcastCommitIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);

        Async async = context.async();
        handler.broadcastCommit(new Zxid(0, 0))
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));

        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestBroadcastCommitFails(final TestContext context) {
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);
        Async async = context.async();
        handler.broadcastCommit(new Zxid(0, 0))
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                context.assertEquals(1, handler.getDeadCoordinationMessages().size());
                async.complete();
            }));

        async.awaitSuccess();
    }

    @Test
    public void TestCreateCoordinationMessage() {
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);

        CoordinationMessage message = handler.createCoordinationMessage(MessagePhase.ACK, MESSAGE, ID);
        Assert.assertEquals(LeaderWriteHandler.TYPE, message.getMetadata().getType());
        Assert.assertEquals(MESSAGE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(MessagePhase.ACK.toValue(), message.getDefinition().getString(ShortenedExchange.PHASE_KEY));
        Assert.assertEquals(ZxidTest.EPOCH, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.EPOCH_KEY).intValue());
        Assert.assertEquals(ZxidTest.COUNTER, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.COUNTER_KEY).intValue());

        message = handler.createCoordinationMessage(MessagePhase.COMMIT, MESSAGE, ID);
        Assert.assertEquals(LeaderWriteHandler.TYPE, message.getMetadata().getType());
        Assert.assertEquals(MESSAGE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(MessagePhase.COMMIT.toValue(), message.getDefinition().getString(ShortenedExchange.PHASE_KEY));
        Assert.assertEquals(ZxidTest.EPOCH, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.EPOCH_KEY).intValue());
        Assert.assertEquals(ZxidTest.COUNTER, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.COUNTER_KEY).intValue());

        message = handler.createCoordinationMessage(MessagePhase.ACK, NULL_ID_MESSAGE, ID);
        Assert.assertEquals(LeaderWriteHandler.TYPE, message.getMetadata().getType());
        Assert.assertEquals(String.format("new-message-%s", ID), message.getMetadata().getMessageID());
        Assert.assertEquals(MessagePhase.ACK.toValue(), message.getDefinition().getString(ShortenedExchange.PHASE_KEY));
        Assert.assertEquals(ZxidTest.EPOCH, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.EPOCH_KEY).intValue());
        Assert.assertEquals(ZxidTest.COUNTER, message.getDefinition().getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.COUNTER_KEY).intValue());
    }

    @Test
    public void TestAddAcknowledgementAndCheckForMajority() {
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);

        handler.getProposalTracker().addAcknowledgedProposalTracker(ID);

        Assert.assertFalse(handler.addAcknowledgementAndCheckForMajority(ID));
        Assert.assertTrue(handler.getProposalTracker().existsAcknowledgedProposalTracker(ID));

        Assert.assertTrue(handler.addAcknowledgementAndCheckForMajority(ID));
        Assert.assertFalse(handler.getProposalTracker().existsAcknowledgedProposalTracker(ID));

        Assert.assertFalse(handler.addAcknowledgementAndCheckForMajority(ID));
        Assert.assertFalse(handler.getProposalTracker().existsAcknowledgedProposalTracker(ID));
    }

    @Test
    public void TestAddCommitAndCheckForMajority() {
        RegistryManager manager = createTestRegistryManager();
        LeaderWriteHandler handler = createWriteHandler(manager);

        handler.getProposalTracker().addCommittedProposalTracker(ID);

        Assert.assertFalse(handler.addCommitAcknowledgementAndCheckForMajority(ID));
        Assert.assertTrue(handler.getProposalTracker().existsCommittedProposalTracker(ID));

        Assert.assertTrue(handler.addCommitAcknowledgementAndCheckForMajority(ID));
        Assert.assertFalse(handler.getProposalTracker().existsCommittedProposalTracker(ID));

        Assert.assertFalse(handler.addCommitAcknowledgementAndCheckForMajority(ID));
        Assert.assertFalse(handler.getProposalTracker().existsCommittedProposalTracker(ID));
    }

    private LeaderWriteHandler createWriteHandler(final RegistryManager manager) {
        ServerClient client = createServerClient();
        return new LeaderWriteHandler(MAJORITY, client, manager, new ConcurrentLinkedQueue<>());
    }
}
