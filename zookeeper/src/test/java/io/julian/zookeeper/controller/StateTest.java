package io.julian.zookeeper.controller;

import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.ProposalTest;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StateTest {
    private Vertx vertx;
    private final static int EPOCH = 0;
    private final static int COUNTER = 0;
    private final static Zxid ID = new Zxid(EPOCH, COUNTER);

    private final static String MESSAGE_ID = "random-id";
    private final static ClientMessage POST_MESSAGE = new ClientMessage(HTTPRequest.POST, new JsonObject(), MESSAGE_ID);
    private final static ClientMessage DELETE_MESSAGE = new ClientMessage(HTTPRequest.DELETE, new JsonObject(), MESSAGE_ID);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void TestAddProposalCompletesSuccessfully(final TestContext context) {
        State state = new State(vertx, new MessageStore());

        Assert.assertEquals(0, state.getHistory().size());
        Async async = context.async();
        state.addProposal(Proposal.mapFrom(ProposalTest.JSON))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(1, state.getHistory().size());
                Assert.assertEquals(ProposalTest.JSON.encodePrettily(), state.getHistory().get(0).toJson().encodePrettily());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestSetLastAcceptedIndex() {
        State state = new State(vertx, new MessageStore());
        Assert.assertEquals(0, state.getLastAcceptedIndex());

        int higher = 2;
        int lower = 1;
        state.setLastAcceptedIndex(higher);
        Assert.assertEquals(higher, state.getLastAcceptedIndex());

        state.setLastAcceptedIndex(lower);
        Assert.assertEquals(higher, state.getLastAcceptedIndex());
    }

    @Test
    public void TestDoesExistOutstandingTransaction(final TestContext context) {
        State state = new State(vertx, new MessageStore());
        Proposal smaller = Proposal.mapFrom(ProposalTest.JSON);
        Proposal bigger = Proposal.mapFrom(ProposalTest.JSON);

        int offset = 100;
        bigger.getTransactionId().setCounter(ProposalTest.COUNTER + offset);

        Async async = context.async();
        state.addProposal(smaller)
            .compose(v -> state.addProposal(bigger))
            .onComplete(context.asyncAssertSuccess(v -> {
                async.complete();
            }));

        async.awaitSuccess();

        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER));
        Assert.assertTrue(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));

        state.setLastAcceptedIndex(2);
        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));
    }

    @Test
    public void TestRetrieveStateUpdateReturnsNull() {
        State state = new State(vertx, new MessageStore());
        Assert.assertNull(state.retrieveStateUpdate(ID));
    }

    @Test
    public void TestRetrieveStateUpdateReturnsClientMessage() {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""), ID));
        Assert.assertNotNull(state.retrieveStateUpdate(ID));
    }

    @Test
    public void processStateUpdateTimesOut(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(POST_MESSAGE, ID));

        Zxid higherID = new Zxid(EPOCH, COUNTER + 1);
        Future<Void> update = state.processStateUpdate(higherID);
        Async async = context.async();
        update.onComplete(context.asyncAssertFailure(cause -> {
            context.assertEquals("State update timeout for '" + higherID.toString() + "'", cause.getMessage());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateEventuallySucceeds(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(POST_MESSAGE, ID));

        Zxid higherID = new Zxid(EPOCH, COUNTER + 1);
        state.addProposal(new Proposal(POST_MESSAGE, higherID));
        Future<Void> update = state.processStateUpdate(higherID);
        Async async = context.async();

        vertx.setTimer(2000, v -> state.getHistory().remove(0));
        update.onComplete(context.asyncAssertSuccess(cause -> {
            context.assertEquals(1, messageStore.getNumberOfMessages());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateProcessesPOSTUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(POST_MESSAGE, ID));

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertSuccess(v -> {
            context.assertEquals(1, messageStore.getNumberOfMessages());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateProcessesDELETEUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        messageStore.putMessage(MESSAGE_ID, new JsonObject());
        state.addProposal(new Proposal(DELETE_MESSAGE, ID));

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertSuccess(v -> {
            context.assertEquals(0, messageStore.getNumberOfMessages());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateFailsDELETEUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(DELETE_MESSAGE, ID));

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertFailure(cause -> {
            context.assertEquals(String.format("Server does not contain message with id '%s'", MESSAGE_ID), cause.getMessage());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateFailsPOSTUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        state.addProposal(new Proposal(POST_MESSAGE, ID));
        messageStore.putMessage(MESSAGE_ID, new JsonObject());

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertFailure(cause -> {
            context.assertEquals(String.format("Server already contains message with id '%s'", MESSAGE_ID), cause.getMessage());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @After
    public void after() {
        this.vertx.close();
    }
}
