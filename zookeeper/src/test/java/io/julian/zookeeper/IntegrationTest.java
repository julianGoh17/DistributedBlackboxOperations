package io.julian.zookeeper;

import io.julian.TestMetricsCollector;
import io.julian.ZookeeperAlgorithm;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Stage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IntegrationTest extends AbstractServerBase {
    private final static JsonObject MESSAGE = new JsonObject().put("test", "message");

    @Test
    public void TestServersSelectLeader(final TestContext context) {
        TestServerComponents server1 = setUpZookeeperApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpZookeeperApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        int epoch = 3;
        int counter = 6;
        ZookeeperAlgorithm algorithm1 = (ZookeeperAlgorithm) server2.server.getVerticle().getAlgorithm();
        algorithm1.getState().setLeaderEpoch(epoch);
        algorithm1.getState().setCounter(counter);

        sendElectionMessage(context);
        List<String> labels = Arrays.asList(server2.server.getController().getLabel(), server1.server.getController().getLabel());
        Assert.assertEquals(1,
            labels.stream().filter(label -> label.equals(LeadershipElectionHandler.LEADER_LABEL)).count());
        Assert.assertEquals(1,
            labels.stream().filter(label -> label.equals(LeadershipElectionHandler.FOLLOWER_LABEL)).count());

        Arrays.asList(server2, server1)
            .forEach(server -> {
                ZookeeperAlgorithm algorithm = (ZookeeperAlgorithm) server.server.getVerticle().getAlgorithm();
                Assert.assertEquals(epoch, algorithm.getState().getLeaderEpoch());
                Assert.assertEquals(counter, algorithm.getState().getCounter());
                assertNoDeadLetters(context, server);
            });
        collector.testHasExpectedStatusSize(6);
        tearDownServer(context, server1);
        tearDownServer(context, server2);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestLeaderWriteRequest(final TestContext context) {
        TestServerComponents server1 = setUpZookeeperApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpZookeeperApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        sendElectionMessage(context);

        TestServerComponents leader = findLeader(server1, server2);
        context.assertNotNull(leader);
        TestClient client = createTestClient();
        Async async = context.async();
        client.POST_MESSAGE(leader.configuration.getHost(), leader.configuration.getPort(), MESSAGE)
            .onComplete(v -> vertx.setTimer(4000, v2 -> {
                Assert.assertEquals(1, server1.server.getMessages().getNumberOfMessages());
                Assert.assertEquals(1, server2.server.getMessages().getNumberOfMessages());
                assertNoDeadLetters(context, server1);
                assertNoDeadLetters(context, server2);
                async.complete();
            }));
        async.awaitSuccess();
        collector.testHasExpectedStatusSize(8);
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server1);
        tearDownServer(context, server2);
    }

    @Test(timeout = 300000)
    public void TestFollowerWriteRequestForwardsToLeader(final TestContext context) {
        TestServerComponents server1 = setUpZookeeperApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpZookeeperApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        sendElectionMessage(context);

        TestServerComponents follower = findFollower(server1, server2);
        context.assertNotNull(follower);
        TestClient client = createTestClient();
        Async async = context.async();
        client.POST_MESSAGE(follower.configuration.getHost(), follower.configuration.getPort(), MESSAGE)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(2500, v2 -> {
                Assert.assertEquals(1, server1.server.getMessages().getNumberOfMessages());
                Assert.assertEquals(1, server2.server.getMessages().getNumberOfMessages());
                assertNoDeadLetters(context, server1);
                assertNoDeadLetters(context, server2);
                async.complete();
            })));
        async.awaitSuccess();
        collector.testHasExpectedStatusSize(8);
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server1);
        tearDownServer(context, server2);
    }

    @Test
    public void TestLeaderAddToDeadLetter(final TestContext context) {
        TestServerComponents server1 = setUpZookeeperApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpZookeeperApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector metricsCollector = setUpMetricsCollector(context);
        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        sendElectionMessage(context);

        TestServerComponents leader = findLeader(server1, server2);
        TestServerComponents follower = findFollower(server1, server2);
        context.assertNotNull(leader);
        context.assertNotNull(follower);
        tearDownServer(context, follower);
        TestClient client = createTestClient();
        Async async = context.async();
        client.POST_MESSAGE(leader.configuration.getHost(), leader.configuration.getPort(), MESSAGE)
            .onComplete(v -> vertx.setTimer(4000, v2 -> {
                Assert.assertEquals(1, leader.server.getMessages().getNumberOfMessages());
                Assert.assertEquals(1, leader.server.getVerticle().getAlgorithm().getController().getNumberOfDeadClientLetters());
                async.complete();
            }));
        async.awaitSuccess();
        metricsCollector.testHasExpectedStatusSize(7);
        tearDownServer(context, leader);
        metricsCollector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestServerRetriesFailedMessages(final TestContext context) {
        TestServerComponents server1 = setUpZookeeperApiServer(context, DEFAULT_SEVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);
        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        sendElectionMessage(context);

        Assert.assertEquals(1, server1.server.getVerticle().getAlgorithm().getDeadCoordinationQueue().size());
        TestServerComponents server2 = setUpZookeeperApiServer(context, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);

        Async async = context.async();
        vertx.setTimer(2000, v -> {
            List<String> labels = Arrays.asList(server2.server.getController().getLabel(), server1.server.getController().getLabel());
            Assert.assertEquals(1,
                labels.stream().filter(label -> label.equals(LeadershipElectionHandler.LEADER_LABEL)).count());
            Assert.assertEquals(1,
                labels.stream().filter(label -> label.equals(LeadershipElectionHandler.FOLLOWER_LABEL)).count());
            async.complete();
            assertNoDeadLetters(context, server1);
            assertNoDeadLetters(context, server2);
        });
        async.awaitSuccess();
        collector.testHasExpectedStatusSize(6);
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server1);
        tearDownServer(context, server2);
    }

    private void sendElectionMessage(final TestContext context, final TestServerComponents... components) {
        Async async = context.async();
        ServerClient client = createServerClient();

        client.sendCoordinateMessageToServer(AbstractServerBase.DEFAULT_SEVER_CONFIG, new CoordinationMessage(HTTPRequest.POST, new JsonObject()))
            .onComplete(context.asyncAssertSuccess(res ->
                // Wait 2 seconds to let servers stabilize
                vertx.setTimer(2000, complete -> {
                    for (TestServerComponents server : components) {
                        ZookeeperAlgorithm algo = (ZookeeperAlgorithm) server.server.getVerticle().getAlgorithm();
                        Assert.assertEquals(Stage.WRITE, algo.getState().getServerStage());
                    }
                    async.complete();
                })));
        async.awaitSuccess();
    }

    private void registerConfigurationInServer(final TestServerComponents components, final ServerConfiguration configuration) {
        components.server.getVerticle().getAlgorithm().getRegistryManager().registerServer(configuration.getHost(), configuration.getPort());
    }

    private TestServerComponents findLeader(final TestServerComponents... components) {
        for (TestServerComponents component : components) {
            if (component.server.getController().getLabel().equals(LeadershipElectionHandler.LEADER_LABEL)) {
                return component;
            }
        }
        return null;
    }

    private TestServerComponents findFollower(final TestServerComponents... components) {
        for (TestServerComponents component : components) {
            if (component.server.getController().getLabel().equals(LeadershipElectionHandler.FOLLOWER_LABEL)) {
                return component;
            }
        }
        return null;
    }

    private void assertNoDeadLetters(final TestContext context, final TestServerComponents server) {
        context.assertEquals(0, server.server.getController().getNumberOfDeadCoordinationLetters());
    }
}
