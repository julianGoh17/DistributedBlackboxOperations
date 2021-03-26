package io.julian.verticle;

import io.julian.MessageHandler;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class RetryVerticleTest extends AbstractServerBase {
    private final AtomicReference<String> deploymentID = new AtomicReference<>();

    @Test
    public void TestRetryVerticleFailsCoordinationMessage(final TestContext context) {
        ConcurrentLinkedQueue<CoordinationMessage> messages = new ConcurrentLinkedQueue<>();
        RetryVerticle verticle = createVerticle(messages, null);
        deployVerticle(context, verticle);
        messages.add(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", ""), new JsonObject(), new JsonObject()));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        verticle.retryCoordinationMessages();
        Async async = context.async();
        vertx.setTimer(3500, id -> {
            vertx.eventBus().publish(getVerticleAddress(), "");
            Assert.assertTrue(verticle.getFailedConsecutiveRequests() > 0);
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestRetryVerticleCoordinationMessageIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        ConcurrentLinkedQueue<CoordinationMessage> messages = new ConcurrentLinkedQueue<>();
        RetryVerticle verticle = createVerticle(messages, null);
        deployVerticle(context, verticle);
        messages.add(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", ""), new JsonObject(), new JsonObject()));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        verticle.retryCoordinationMessages();
        Async async = context.async();
        vertx.setTimer(3500, id -> {
            vertx.eventBus().publish(getVerticleAddress(), "");
            Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());
            async.complete();
        });
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestRetryVerticleFailsClientMessage(final TestContext context) {
        ConcurrentLinkedQueue<ClientMessage> messages = new ConcurrentLinkedQueue<>();
        RetryVerticle verticle = createVerticle(null, messages);
        deployVerticle(context, verticle);
        messages.add(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        verticle.retryClientMessages();
        Async async = context.async();
        vertx.setTimer(3500, id -> {
            vertx.eventBus().publish(getVerticleAddress(), "");
            Assert.assertTrue(verticle.getFailedConsecutiveRequests() > 0);
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestRetryVerticleSucceedsClientMessage(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        ConcurrentLinkedQueue<ClientMessage> messages = new ConcurrentLinkedQueue<>();
        RetryVerticle verticle = createVerticle(null, messages);
        deployVerticle(context, verticle);
        messages.add(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        verticle.retryClientMessages();
        Async async = context.async();
        vertx.setTimer(3500, id -> {
            vertx.eventBus().publish(getVerticleAddress(), "");
            Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());
            async.complete();
        });
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestFormatAddress() {
        RetryVerticle verticle = createVerticle();
        Assert.assertEquals("retry-random-0", verticle.formatAddress("random"));
    }

    @Test
    public void TestGetTimeout() {
        RetryVerticle verticle = createVerticle();
        Assert.assertEquals(1000, verticle.getTimeout(0), 0);
        Assert.assertEquals(2000, verticle.getTimeout(1), 0);
        Assert.assertEquals(4000, verticle.getTimeout(2), 0);
        Assert.assertEquals(1024000, verticle.getTimeout(10), 0);
    }

    private RetryVerticle createVerticle() {
        return createVerticle(null, null);
    }

    private RetryVerticle createVerticle(final ConcurrentLinkedQueue<CoordinationMessage> coordination, final ConcurrentLinkedQueue<ClientMessage> client) {
        final ConcurrentLinkedQueue<CoordinationMessage> coordinationQueue = Optional.ofNullable(coordination)
            .orElse(new ConcurrentLinkedQueue<>());
        final ConcurrentLinkedQueue<ClientMessage> clientQueue = Optional.ofNullable(client)
            .orElse(new ConcurrentLinkedQueue<>());
        MessageHandler handler = createHandler();
        handler.getController().setLabel(LeadershipElectionHandler.LEADER_LABEL);
        return new RetryVerticle(handler, vertx, coordinationQueue, clientQueue);
    }

    private MessageHandler createHandler() {
        return new MessageHandler(new Controller(new Configuration()), new MessageStore(), vertx, createTestRegistryManager(), createServerClient(), new ConcurrentLinkedQueue<>());
    }

    private void deployVerticle(final TestContext context, final RetryVerticle verticle) {
        Async async = context.async();
        deployHelper(verticle)
            .onComplete(context.asyncAssertSuccess(id -> {
                deploymentID.set(id);
                async.complete();
            }));
        async.awaitSuccess();
    }

    private <T extends AbstractVerticle> Future<String> deployHelper(final T verticle) {
        Promise<String> deployment = Promise.promise();
        vertx.deployVerticle(verticle, res -> {
            if (res.succeeded()) {
                deployment.complete(res.result());
            } else {
                deployment.fail("Could not deploy");
            }
        });
        return deployment.future();
    }


    private String getVerticleAddress() {
        return String.format("%s-%s-0", RetryVerticle.VERTICLE_ADDRESS, RetryVerticle.STOP_POSTFIX);
    }
}
