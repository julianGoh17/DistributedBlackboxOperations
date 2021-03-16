package io.julian.zookeeper.synchronize;

import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FollowerSynchronizeHandlerTest extends AbstractServerBase {
    private static final int INITIALIZED_MESSAGES = 5;
    private static final Zxid EARLIEST_ID = new Zxid(0, 1);
    private static final Zxid LATEST_ID = new Zxid(12, 1);

    private static final JsonObject POST = new JsonObject().put("random", "key");

    @Test
    public void TestSynchronizeWithLeaderState(final TestContext context) {
        FollowerSynchronizeHandler handler = createTestHandler();
        State leader = createInitializedState(context);

        Async async = context.async();
        handler.synchronizeWithLeaderState(leader)
            .onComplete(context.asyncAssertSuccess(v -> {
                context.assertEquals(INITIALIZED_MESSAGES, handler.getState().getHistory().size());
                boolean isSorted = true;
                for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
                    if (i > 0 && !handler.getState().getHistory().get(i).getTransactionId().isLaterThan(handler.getState().getHistory().get(i - 1).getTransactionId())) {
                        isSorted = false;
                    }
                    Assert.assertEquals(leader.getHistory().get(i).getTransactionId(), handler.getState().getHistory().get(i).getTransactionId());
                }
                Assert.assertEquals(leader.getLeaderEpoch(), handler.getState().getLeaderEpoch());
                Assert.assertEquals(leader.getCounter(), handler.getState().getCounter());
                Assert.assertEquals(leader.getLastAcceptedIndex(), handler.getState().getLastAcceptedIndex());
                Assert.assertTrue(isSorted);
                async.complete();
            }));
        async.awaitSuccess();
        Assert.assertEquals(leader.getMessageStore().getNumberOfMessages(), handler.getState().getMessageStore().getNumberOfMessages());
        for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
            Assert.assertEquals(
                leader.getMessageStore().getMessage(Integer.toString(i)).encodePrettily(),
                handler.getState().getMessageStore().getMessage(Integer.toString(i)).encodePrettily());
        }
    }

    @Test
    public void TestFindMissingProposalsWhenEmptyState(final TestContext context) {
        FollowerSynchronizeHandler handler = createTestHandler();
        List<Proposal> proposals = handler.findMissingProposals(createInitializedState(context));

        int expectedMessages = INITIALIZED_MESSAGES;
        Assert.assertEquals(expectedMessages, proposals.size());
        for (int i = 0; i < expectedMessages; i++) {
            Assert.assertEquals(Integer.toString(i), proposals.get(i).getNewState().getMessageId());
            Assert.assertEquals(POST.encodePrettily(), proposals.get(i).getNewState().getMessage().encodePrettily());
        }
    }

    @Test
    public void TestFindMissingProposalsWhenLastAcceptedLessThanTotalList(final TestContext context) {
        int expectedMessages = INITIALIZED_MESSAGES - 2;
        FollowerSynchronizeHandler handler = createTestHandler();
        State leader = createInitializedState(context);
        leader.setLastAcceptedIndex(expectedMessages);
        List<Proposal> proposals = handler.findMissingProposals(leader);

        Assert.assertEquals(expectedMessages, proposals.size());
        for (int i = 0; i < expectedMessages; i++) {
            Assert.assertEquals(Integer.toString(i), proposals.get(i).getNewState().getMessageId());
            Assert.assertEquals(POST.encodePrettily(), proposals.get(i).getNewState().getMessage().encodePrettily());
        }
    }

    private FollowerSynchronizeHandler createTestHandler() {
        return new FollowerSynchronizeHandler(vertx, new State(vertx, new MessageStore()), createTestCandidateInformationRegistry(true), createServerClient());
    }

    private State createInitializedState(final TestContext context) {
        State state = new State(vertx, new MessageStore());
        List<Future> stateUpdate = new ArrayList<>();
        for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
            ClientMessage message = new ClientMessage(HTTPRequest.POST, POST, Integer.toString(i));
            stateUpdate.add(state.addProposal(new Proposal(message, new Zxid(EARLIEST_ID.getEpoch(), i + 1))));
            state.getMessageStore().putMessage(message.getMessageId(), message.getMessage());
        }
        Async async = context.async();
        CompositeFuture.all(stateUpdate)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        state.setCounter(INITIALIZED_MESSAGES - 1);
        state.setLeaderEpoch(1);
        state.setLastAcceptedIndexIfGreater(INITIALIZED_MESSAGES - 1);
        return state;
    }
}
