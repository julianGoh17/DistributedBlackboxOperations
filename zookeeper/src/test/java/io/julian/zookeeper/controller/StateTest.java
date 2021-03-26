package io.julian.zookeeper.controller;

import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.ProposalTest;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

@RunWith(VertxUnitRunner.class)
public class StateTest {
    private Vertx vertx;
    private final static int EPOCH = 0;
    private final static int COUNTER = 0;
    private final static Zxid ID = new Zxid(EPOCH, COUNTER);
    private final static Zxid LATER_ID = new Zxid(EPOCH, COUNTER + 1);

    private final static String MESSAGE_ID = "random-id";
    private final static ClientMessage POST_MESSAGE = new ClientMessage(HTTPRequest.POST, new JsonObject(), MESSAGE_ID);
    private final static ClientMessage DELETE_MESSAGE = new ClientMessage(HTTPRequest.DELETE, new JsonObject(), MESSAGE_ID);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @Test
    public void TestInit() {
        State state = new State(vertx, new MessageStore());
        Assert.assertEquals(0, state.getHistory().size());
        Assert.assertEquals(0, state.getCounter());
        Assert.assertEquals(0, state.getLastAcceptedIndex());
        Assert.assertEquals(0, state.getLeaderEpoch());
        Assert.assertEquals(0, state.getMessageStore().getNumberOfMessages());

        state = new State(new ArrayList<>(), 0, 0, 0);
        Assert.assertEquals(0, state.getHistory().size());
        Assert.assertEquals(0, state.getCounter());
        Assert.assertEquals(0, state.getLastAcceptedIndex());
        Assert.assertEquals(0, state.getLeaderEpoch());
        Assert.assertNull(state.getMessageStore());
    }

    @Test
    public void TestGetterAndSetter() {
        State state = new State(vertx, new MessageStore());
        int leaderEpoch = 15;
        int counter = 1234;
        state.setLeaderEpoch(leaderEpoch);
        state.setCounter(counter);

        Assert.assertEquals(leaderEpoch, state.getLeaderEpoch());
        Assert.assertEquals(counter + 1, state.incrementAndGetCounter());
        Assert.assertEquals(counter + 1, state.getCounter());
    }

    @Test
    public void TestAddProposalCompletesSuccessfully(final TestContext context) {
        State state = new State(vertx, new MessageStore());

        Assert.assertEquals(0, state.getHistory().size());
        addProposal(context, state, Proposal.mapFrom(ProposalTest.JSON));
        Assert.assertEquals(1, state.getHistory().size());
        Assert.assertEquals(ProposalTest.JSON.encodePrettily(), state.getHistory().get(0).toJson().encodePrettily());
    }

    @Test
    public void TestAddProposalCompletesDoesNotAddSameID(final TestContext context) {
        State state = new State(vertx, new MessageStore());

        Assert.assertEquals(0, state.getHistory().size());
        addProposal(context, state, Proposal.mapFrom(ProposalTest.JSON));
        Assert.assertEquals(1, state.getHistory().size());
        addProposal(context, state, Proposal.mapFrom(ProposalTest.JSON));
        Assert.assertEquals(1, state.getHistory().size());
        Assert.assertEquals(ProposalTest.JSON.encodePrettily(), state.getHistory().get(0).toJson().encodePrettily());
    }

    @Test
    public void TestSetLastAcceptedIfGreaterIndex() {
        State state = new State(vertx, new MessageStore());
        Assert.assertEquals(0, state.getLastAcceptedIndex());

        int higher = 2;
        int lower = 1;
        state.setLastAcceptedIndexIfGreater(higher);
        Assert.assertEquals(higher + 1, state.getLastAcceptedIndex());

        state.setLastAcceptedIndexIfGreater(lower);
        Assert.assertEquals(higher + 1, state.getLastAcceptedIndex());
    }

    @Test
    public void TestDoesExistOutstandingTransaction(final TestContext context) {
        State state = new State(vertx, new MessageStore());
        Proposal smaller = Proposal.mapFrom(ProposalTest.JSON);
        Proposal bigger = Proposal.mapFrom(ProposalTest.JSON);

        int offset = 100;
        bigger.getTransactionId().setCounter(ProposalTest.COUNTER + offset);

        addProposal(context, state, smaller);
        addProposal(context, state, bigger);

        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER));
        Assert.assertTrue(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));

        state.setLastAcceptedIndexIfGreater(2);
        Assert.assertFalse(state.doesExistOutstandingTransaction(ProposalTest.COUNTER + offset));
    }

    @Test
    public void TestRetrieveStateUpdateIndexReturnsInvalidWhenNotFound() {
        State state = new State(vertx, new MessageStore());
        State.LastIndexResult res = state.retrieveStateUpdateIndex(ID);
        Assert.assertEquals(-1, res.index);
    }

    @Test
    public void TestRetrieveStateUpdateIndexReturnsSuccessfully(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""), ID));
        State.LastIndexResult res = state.retrieveStateUpdateIndex(ID);
        Assert.assertEquals(0, res.index);
        Assert.assertTrue(res.canUpdate);
    }

    @Test
    public void TestRetrieveStateUpdateIndexReturnsSuccessfullyButCannotUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""), LATER_ID));
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""), ID));
        State.LastIndexResult res = state.retrieveStateUpdateIndex(ID);
        Assert.assertEquals(1, res.index);
        Assert.assertFalse(res.canUpdate);
    }

    @Test
    public void TestStateUpdatesEvenIfMessagesOutOfOrder(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), LATER_ID.toString()), LATER_ID));
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ID.toString()), ID));
        Async async = context.async();
        state.processStateUpdate(ID)
            .compose(v -> state.processStateUpdate(LATER_ID))
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(1, state.getLastAcceptedIndex());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestRetrieveStateUpdateReturnsClientMessage(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""), ID));
        Assert.assertNotNull(state.retrieveStateUpdate(0));
    }

    @Test
    public void processStateUpdateTimesOut(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(POST_MESSAGE, ID));

        Future<Void> update = state.processStateUpdate(LATER_ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertFailure(cause -> {
            context.assertEquals("State update timeout for '" + LATER_ID.toString() + "'", cause.getMessage());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void processStateUpdateEventuallySucceeds(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(POST_MESSAGE, ID));
        addProposal(context, state, new Proposal(POST_MESSAGE, LATER_ID));

        Future<Void> update = state.processStateUpdate(LATER_ID);
        Async async = context.async();

        vertx.setTimer(500, v -> state.getHistory().remove(0));
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
        addProposal(context, state, new Proposal(POST_MESSAGE, ID));

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertSuccess(v -> {
            context.assertEquals(1, messageStore.getNumberOfMessages());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void TestProcessStateUpdateProcessesDELETEUpdate(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        messageStore.putMessage(MESSAGE_ID, new JsonObject());
        addProposal(context, state, new Proposal(DELETE_MESSAGE, ID));

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
        addProposal(context, state, new Proposal(DELETE_MESSAGE, ID));


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
        addProposal(context, state, new Proposal(POST_MESSAGE, ID));

        messageStore.putMessage(MESSAGE_ID, new JsonObject());

        Future<Void> update = state.processStateUpdate(ID);
        Async async = context.async();
        update.onComplete(context.asyncAssertFailure(cause -> {
            context.assertEquals(String.format("Server already contains message with id '%s'", MESSAGE_ID), cause.getMessage());
            async.complete();
        }));
        async.awaitSuccess();
    }

    @Test
    public void TestToJson(final TestContext context) {
        MessageStore messageStore = new MessageStore();
        State state = new State(vertx, messageStore);
        addProposal(context, state, new Proposal(POST_MESSAGE, ID));
        JsonArray expectedArray = new JsonArray();
        expectedArray.add(new Proposal(POST_MESSAGE, ID).toJson());
        JsonObject json = state.toJson();
        Assert.assertEquals(0, json.getInteger(State.LEADER_EPOCH_KEY).intValue());
        Assert.assertEquals(expectedArray.encodePrettily(), json.getJsonArray(State.HISTORY_KEY).encodePrettily());
        Assert.assertEquals(0, json.getInteger(State.COUNTER_KEY).intValue());
        Assert.assertEquals(0, json.getInteger(State.LAST_ACCEPTED_INDEX_KEY).intValue());
    }

    @Test
    public void TestMapFromJson() {
        JsonArray history = new JsonArray();
        int leaderEpoch = 3;
        int counter = -1;
        int lastAcceptedIndex = 5;
        history.add(new Proposal(POST_MESSAGE, ID).toJson());
        JsonObject json = new JsonObject()
            .put(State.LEADER_EPOCH_KEY, leaderEpoch)
            .put(State.COUNTER_KEY, counter)
            .put(State.HISTORY_KEY, history)
            .put(State.LAST_ACCEPTED_INDEX_KEY, lastAcceptedIndex);

        State state = State.fromJson(json);
        Assert.assertEquals(leaderEpoch, state.getLeaderEpoch());
        Assert.assertEquals(1, state.getHistory().size());
        Assert.assertEquals(POST_MESSAGE.toJson().encodePrettily(), state.getHistory().get(0).getNewState().toJson().encodePrettily());
        Assert.assertEquals(ID.toJson().encodePrettily(), state.getHistory().get(0).getTransactionId().toJson().encodePrettily());
        Assert.assertEquals(counter, state.getCounter());
        Assert.assertEquals(lastAcceptedIndex, state.getLastAcceptedIndex());
    }

    @Test
    public void TestIsLaterThan() {
        State lowest = new State(Collections.emptyList(), 0, 0, 0);
        State middle = new State(Collections.emptyList(), 0, 1, 10);
        State highest = new State(Collections.emptyList(), 0, 2, 0);

        Assert.assertTrue(highest.isLaterThanState(middle));
        Assert.assertTrue(highest.isLaterThanState(lowest));
        Assert.assertTrue(middle.isLaterThanState(lowest));

        Assert.assertFalse(middle.isLaterThanState(highest));
        Assert.assertFalse(lowest.isLaterThanState(middle));
        Assert.assertFalse(lowest.isLaterThanState(highest));
    }

    @Test
    public void TestSetState() {
        State state = new State(Collections.emptyList(), 0, 0, 0);
        State other = new State(Collections.singletonList(new Proposal(null, null)), 4, 1, 10);

        Assert.assertNotEquals(state, other);
        state.setState(other);
        Assert.assertEquals(1, state.getHistory().size());
        Assert.assertNull(state.getHistory().get(0).getNewState());
        Assert.assertNull(state.getHistory().get(0).getTransactionId());
        Assert.assertEquals(other.getLeaderEpoch(), state.getLeaderEpoch());
        Assert.assertEquals(other.getCounter(), state.getCounter());
        Assert.assertEquals(other.getLastAcceptedIndex(), state.getLastAcceptedIndex());
        Assert.assertNotEquals(state, other);
    }

    @After
    public void after() {
        this.vertx.close();
    }

    private void addProposal(final TestContext context, final State state, final Proposal proposal) {
        Async async = context.async();
        state.addProposal(proposal)
            .onComplete(v -> async.complete());
        async.awaitSuccess();
    }
}
