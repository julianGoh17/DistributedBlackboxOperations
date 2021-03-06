package io.julian.zookeeper.discovery;

import io.julian.TestMetricsCollector;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.Zxid;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class FollowerDiscoveryHandlerTest extends AbstractServerBase {
    @Test
    public void TestCreateCoordinationMessage() {
        CandidateInformationRegistry registry = createTestCandidateInformationRegistry(false);
        FollowerDiscoveryHandler handler = getTestHandler(registry);
        CoordinationMessage message = handler.createCoordinationMessage();
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals(FollowerDiscoveryHandler.FOLLOWER_DISCOVERY_MESSAGE_ID, message.getMetadata().getMessageID());
        Assert.assertEquals(DiscoveryHandler.DISCOVERY_TYPE, message.getMetadata().getType());
        Assert.assertNull(message.getMessage());
        Assert.assertEquals(new State(vertx, new MessageStore()).toJson().encodePrettily(), message.getDefinition().encodePrettily());
    }

    @Test
    public void TestReplyToLeaderIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        CandidateInformationRegistry registry = createTestCandidateInformationRegistry(true);
        FollowerDiscoveryHandler handler = getTestHandler(registry);

        Async async = context.async();
        handler.replyToLeader()
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> async.complete())));
        async.awaitSuccess();
        tearDownServer(context, server);
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestReplyToLeaderFails(final TestContext context) {
        CandidateInformationRegistry registry = createTestCandidateInformationRegistry(true);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        FollowerDiscoveryHandler handler = getTestHandler(registry);

        Async async = context.async();
        handler.replyToLeader()
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(1, handler.getDeadCoordinationMessages().size());
                vertx.setTimer(1000, v1 -> {
                    metricsCollector.testHasExpectedStatusSize(1);
                    async.complete();
                });
            }));
        async.awaitSuccess();
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestUpdateToLeaderState() {
        CandidateInformationRegistry registry = createTestCandidateInformationRegistry(true);
        FollowerDiscoveryHandler handler = getTestHandler(registry);
        Assert.assertEquals(0, handler.getState().getLeaderEpoch());
        Assert.assertEquals(0, handler.getState().getCounter());

        int leaderEpoch = -1;
        int counter = 3;
        handler.updateToLeaderState(new Zxid(leaderEpoch, counter));
        Assert.assertEquals(leaderEpoch, handler.getState().getLeaderEpoch());
        Assert.assertEquals(counter, handler.getState().getCounter());
    }

    private FollowerDiscoveryHandler getTestHandler(final CandidateInformationRegistry registry) {
        return new FollowerDiscoveryHandler(new State(vertx, new MessageStore()), registry, createServerClient(), new ConcurrentLinkedQueue<>());
    }
}
