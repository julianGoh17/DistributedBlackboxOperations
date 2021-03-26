package io.julian.zookeeper.synchronize;

import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Stage;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SynchronizeHandlerTest extends AbstractServerBase {
    @Test
    public void TestHasReceivedEnoughAcknowledgements() {
        SynchronizeHandler handler = createTestHandler(true);
        Assert.assertFalse(handler.hasReceivedEnoughAcknowledgements());
        handler.handleCoordinationMessage(new CoordinationMessage(HTTPRequest.POST, new JsonObject()));
        Assert.assertTrue(handler.hasReceivedEnoughAcknowledgements());
    }

    @Test
    public void TestHandleCoordinationMessageAsLeader(final TestContext context) {
        SynchronizeHandler handler = createTestHandler(true);
        Assert.assertFalse(handler.hasReceivedEnoughAcknowledgements());
        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(HTTPRequest.POST, new JsonObject()))
            .onComplete(context.asyncAssertSuccess(v -> {
                Assert.assertTrue(handler.hasReceivedEnoughAcknowledgements());
                Assert.assertEquals(Stage.WRITE, handler.getState().getServerStage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestHandleCoordinationMessageAsFollowerSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        SynchronizeHandler handler = createTestHandler(false);
        State state = createInitializedState(context);
        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(HTTPRequest.POST, state.toJson()))
            .onComplete(context.asyncAssertSuccess(v -> {
                Assert.assertEquals(1, handler.getLeaderSynchronizeHandler().getState().getHistory().size());
                Assert.assertEquals(Stage.WRITE, handler.getState().getServerStage());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsFollowerFails(final TestContext context) {
        SynchronizeHandler handler = createTestHandler(false);
        State state = createInitializedState(context);
        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(HTTPRequest.POST, state.toJson()))
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(1, handler.getLeaderSynchronizeHandler().getState().getHistory().size());
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(Stage.SYNCHRONIZE, handler.getState().getServerStage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    private SynchronizeHandler createTestHandler(final boolean isLeader) {
        Controller controller = new Controller(new Configuration());
        controller.setLabel(isLeader ? LeadershipElectionHandler.LEADER_LABEL : LeadershipElectionHandler.FOLLOWER_LABEL);
        State state = new State(vertx, new MessageStore());
        state.setServerStage(Stage.SYNCHRONIZE);
        return new SynchronizeHandler(vertx, state, createTestRegistryManager(),
            createServerClient(), createTestCandidateInformationRegistry(!isLeader), controller, new ConcurrentLinkedQueue<>());
    }

    private State createInitializedState(final TestContext context) {
        State state = new State(vertx, new MessageStore());
        state.setServerStage(Stage.SYNCHRONIZE);
        Async async = context.async();
        state.addProposal(new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), "0"), new Zxid(0, 1)))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));

        async.awaitSuccess();
        return state;
    }
}
