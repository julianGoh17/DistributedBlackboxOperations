package io.julian.server.api;

import io.julian.server.components.Controller;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class DistributedAlgorithmVerticleTest {
    private Vertx vertx;
    private Controller controller;
    private DistributedAlgorithmVerticle verticle;
    private AtomicReference<String> deploymentID = new AtomicReference<>();

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        vertx.close();
    }

    @Test
    public void TestCanCommunicateWithVerticle(final TestContext context) {
        setUpTest(context);
        int messages = 5;
        for (int i = 0; i < messages; i++) {
            vertx.eventBus().send(DistributedAlgorithmVerticle.formatAddress(DistributedAlgorithmVerticle.CONSUME_MESSAGE_POSTFIX), "random-message");
        }

        Promise<Void> timerComplete = Promise.promise();
        vertx.setTimer(1000, v -> timerComplete.complete());

        Async async = context.async();
        timerComplete.future().onComplete(context.asyncAssertSuccess(v -> {
            Assert.assertEquals(messages, controller.getNumberOfCoordinationMessages());
            async.complete();
        }));

        async.awaitSuccess();
        tearDownTest(context);
    }

    private void setUpTest(final TestContext context) {
        controller = new Controller();
        DistributedAlgorithmTest.ExampleAlgorithm algorithm = new DistributedAlgorithmTest.ExampleAlgorithm(controller);
        verticle = new DistributedAlgorithmVerticle(algorithm);

        Async async = context.async();
        vertx.deployVerticle(verticle, context.asyncAssertSuccess(res -> {
            deploymentID.set(res);
            async.complete();
        }));

        async.awaitSuccess();
    }

    private void tearDownTest(final TestContext context) {
        Async async = context.async();
        vertx.undeploy(deploymentID.get(), context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
    }
}
