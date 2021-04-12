package io.julian.zookeeper.synchronize;

import io.julian.TestMetricsCollector;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FollowerSynchronizeHandlerTest extends AbstractServerBase {
    private static final int INITIALIZED_MESSAGES = 5;
    private static final Zxid EARLIEST_ID = new Zxid(0, 1);
    private static final JsonObject POST = new JsonObject().put("random", "key");

    @Test
    public void TestReplyToLeaderIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        FollowerSynchronizeHandler handler = createTestHandler();
        State leader = createInitializedState(context);
        Async async = context.async();
        handler.replyToLeader(leader)
            .onComplete(context.asyncAssertSuccess(v1 -> vertx.setTimer(1000, v -> {
                checkHandlerHasSameState(context, handler.getState(), leader);
                async.complete();
            })));
        async.awaitSuccess();
        checkMessageStoreIsTheSame(leader.getMessageStore(), handler.getState().getMessageStore());
        tearDownServer(context, server);
        metricsCollector.testHasExpectedStatusSize(1);
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestReplyToLeaderFails(final TestContext context) {
        FollowerSynchronizeHandler handler = createTestHandler();
        State leader = createInitializedState(context);
        Async async = context.async();
        handler.replyToLeader(leader)
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(1, handler.getDeadCoordinationMessages().size());
                checkHandlerHasSameState(context, handler.getState(), leader);
                async.complete();
            }));
        async.awaitSuccess();
        checkMessageStoreIsTheSame(leader.getMessageStore(), handler.getState().getMessageStore());
    }

    @Test
    public void TestSynchronizeWithLeaderState(final TestContext context) {
        FollowerSynchronizeHandler handler = createTestHandler();
        State leader = createInitializedState(context);

        Async async = context.async();
        handler.synchronizeWithLeaderState(leader)
            .onComplete(context.asyncAssertSuccess(v -> {
                checkHandlerHasSameState(context, handler.getState(), leader);
                async.complete();
            }));
        async.awaitSuccess();
        checkMessageStoreIsTheSame(leader.getMessageStore(), handler.getState().getMessageStore());
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
        return new FollowerSynchronizeHandler(vertx, new State(vertx, new MessageStore()), createTestCandidateInformationRegistry(true), createServerClient(), new ConcurrentLinkedQueue<>());
    }

    private void checkHandlerHasSameState(final TestContext context, final State handler, final State leader) {
        context.assertEquals(INITIALIZED_MESSAGES, handler.getHistory().size());
        boolean isSorted = true;
        for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
            if (i > 0 && !handler.getHistory().get(i).getTransactionId().isLaterThan(handler.getHistory().get(i - 1).getTransactionId())) {
                isSorted = false;
            }
            Assert.assertEquals(leader.getHistory().get(i).getTransactionId(), handler.getHistory().get(i).getTransactionId());
        }
        Assert.assertEquals(leader.getLeaderEpoch(), handler.getLeaderEpoch());
        Assert.assertEquals(leader.getCounter(), handler.getCounter());
        Assert.assertEquals(leader.getLastAcceptedIndex(), handler.getLastAcceptedIndex());
        Assert.assertTrue("handler history is not in sorted order", isSorted);
    }

    private void checkMessageStoreIsTheSame(final MessageStore leader, final MessageStore handler) {
        Assert.assertEquals(leader.getNumberOfMessages(), handler.getNumberOfMessages());
        for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
            Assert.assertEquals(
                leader.getMessage(Integer.toString(i)).encodePrettily(),
                handler.getMessage(Integer.toString(i)).encodePrettily());
        }
    }

    private State createInitializedState(final TestContext context) {
        State state = new State(vertx, new MessageStore());
        for (int i = 0; i < INITIALIZED_MESSAGES; i++) {
            ClientMessage message = new ClientMessage(HTTPRequest.POST, POST, Integer.toString(i));
            addProposal(context, state, new Proposal(message, new Zxid(EARLIEST_ID.getEpoch(), i + 1)));
            state.getMessageStore().putMessage(message.getMessageId(), message.getMessage());
        }

        state.setCounter(INITIALIZED_MESSAGES - 1);
        state.setLeaderEpoch(1);
        state.setLastAcceptedIndex(INITIALIZED_MESSAGES);
        return state;
    }

    private void addProposal(final TestContext context, final State state, final Proposal proposal) {
        Async async = context.async();
        state.addProposal(proposal)
            .onComplete(v -> async.complete());
        async.awaitSuccess();
    }
}
