package io.julian.gossip;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.server.models.control.ServerConfiguration;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;
import tools.TestClient;
import tools.TestMetricsCollector;
import tools.TestServerComponents;

public class IntegrationTest extends AbstractHandlerTest {
    private final static String MESSAGE_ID = "message_id";
    @Test
    public void TestServersPropagatePostMessages(final TestContext context) {
        TestServerComponents server1 = setUpGossipApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpGossipApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);

        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        getGossipConfiguration(server1).setInactiveProbability(1);
        getGossipConfiguration(server2).setInactiveProbability(1);

        sendPostMessage(context);
        Async async = context.async();
        vertx.setTimer(2000, v -> {
            Assert.assertEquals(1, server1.server.getMessages().getNumberOfMessages());
            Assert.assertEquals(1, server2.server.getMessages().getNumberOfMessages());
            collector.testHasExpectedStatusSize(2);
            async.complete();
        });
        async.awaitSuccess();
        tearDownServer(context, server1);
        tearDownServer(context, server2);
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestServersPropagateDeleteMessages(final TestContext context) {
        TestServerComponents server1 = setUpGossipApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpGossipApiServer(context, SECOND_SERVER_CONFIG);
        TestMetricsCollector collector = setUpMetricsCollector(context);

        registerConfigurationInServer(server1, SECOND_SERVER_CONFIG);
        registerConfigurationInServer(server2, DEFAULT_SEVER_CONFIG);
        getGossipConfiguration(server1).setInactiveProbability(1);
        getGossipConfiguration(server2).setInactiveProbability(1);
        addMessageToServer(server1, MESSAGE_ID);
        addMessageToServer(server2, MESSAGE_ID);

        sendDeleteMessage(context);
        Async async = context.async();
        vertx.setTimer(2000, v -> {
            Assert.assertEquals(0, server1.server.getMessages().getNumberOfMessages());
            Assert.assertEquals(0, server2.server.getMessages().getNumberOfMessages());
            collector.testHasExpectedStatusSize(2);
            async.complete();
        });
        async.awaitSuccess();
        tearDownServer(context, server1);
        tearDownServer(context, server2);
        collector.tearDownMetricsCollector(context);
    }

    private void sendPostMessage(final TestContext context) {
        Async async = context.async();
        TestClient client = new TestClient(vertx);

        client.POST_MESSAGE(DEFAULT_SEVER_CONFIG.getHost(), DEFAULT_SEVER_CONFIG.getPort(), new JsonObject())
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
    }

    private void sendDeleteMessage(final TestContext context) {
        Async async = context.async();
        TestClient client = new TestClient(vertx);

        client.DELETE_MESSAGE(DEFAULT_SEVER_CONFIG.getHost(), DEFAULT_SEVER_CONFIG.getPort(), MESSAGE_ID)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
    }

    private void registerConfigurationInServer(final TestServerComponents components, final ServerConfiguration configuration) {
        components.server.getVerticle().getAlgorithm().getRegistryManager().registerServer(configuration.getHost(), configuration.getPort());
    }

    private void addMessageToServer(final TestServerComponents components, final String messageId) {
        Gossip gossip = (Gossip) components.server.getVerticle().getAlgorithm();
        gossip.getState().addMessageIfNotInDatabase(messageId, new JsonObject());
    }

    private GossipConfiguration getGossipConfiguration(final TestServerComponents components) {
        Gossip gossip = (Gossip) components.server.getVerticle().getAlgorithm();
        return gossip.getGossipConfiguration();
    }
}
