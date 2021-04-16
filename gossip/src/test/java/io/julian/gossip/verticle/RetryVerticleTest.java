package io.julian.gossip.verticle;

import io.julian.gossip.MessageHandler;
import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.UpdateResponse;
import io.julian.gossip.write.WriteReplyHandler;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;
import tools.TestServerComponents;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class RetryVerticleTest extends AbstractHandlerTest {
    private final AtomicReference<String> deploymentID = new AtomicReference<>();

    private final static String MESSAGE_ID = "id";

    @Test
    public void TestRetryVerticleFailsCoordinationMessage(final TestContext context) {
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        MessageStore messages = new MessageStore();
        messages.putMessage(MESSAGE_ID, new JsonObject());
        RetryVerticle verticle = createVerticle(createHandler(createState(messages)), deadLetters);
        deployVerticle(context, verticle);

        deadLetters.add(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", WriteReplyHandler.WRITE_REPLY_TYPE), new JsonObject(), new UpdateResponse("id", false).toJson()));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        Async async = context.async();
        vertx.setTimer(2500, id -> {
            Assert.assertEquals(1, verticle.getFailedConsecutiveRequests());
            vertx.eventBus().publish(getVerticleAddress(), "");
            async.complete();
        });
        async.awaitSuccess();
    }

    @Test
    public void TestRetryVerticleCoordinationMessageIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        MessageStore messages = new MessageStore();
        messages.putMessage(MESSAGE_ID, new JsonObject());
        RetryVerticle verticle = createVerticle(createHandler(createState(messages)), deadLetters);
        deployVerticle(context, verticle);
        deadLetters.add(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", WriteReplyHandler.WRITE_REPLY_TYPE), new JsonObject(), new UpdateResponse(MESSAGE_ID, true).toJson()));
        Assert.assertEquals(0, verticle.getFailedConsecutiveRequests());

        Async async = context.async();
        vertx.setTimer(2500, id -> {
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
        return createVerticle(null);
    }

    private RetryVerticle createVerticle(final ConcurrentLinkedQueue<CoordinationMessage> coordination) {
        return createVerticle(createHandler(), coordination);
    }

    private RetryVerticle createVerticle(final MessageHandler handler, final ConcurrentLinkedQueue<CoordinationMessage> coordination) {
        final ConcurrentLinkedQueue<CoordinationMessage> coordinationQueue = Optional.ofNullable(coordination)
            .orElse(new ConcurrentLinkedQueue<>());
        return new RetryVerticle(handler, vertx, coordinationQueue);
    }

    private MessageHandler createHandler() {
        return createHandler(createState());
    }

    private MessageHandler createHandler(final State state) {
        return new MessageHandler(createServerClient(), state, createTestRegistryManager(), new GossipConfiguration(), DEFAULT_SEVER_CONFIG, vertx);
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
