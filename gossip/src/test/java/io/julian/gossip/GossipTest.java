package io.julian.gossip;

import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class GossipTest {
    @Test
    public void TestDeployVerticleSucceeds(final TestContext context) {
        Vertx vertx = Vertx.vertx();
        Gossip algorithm = createGossipAlgorithm(vertx);

        Async async = context.async();
        algorithm.deployRetryVerticle()
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        vertx.close();
    }

    @Test
    public void TestDeployVerticle(final TestContext context) {
        Vertx vertx = Vertx.vertx();
        Gossip algorithm = createGossipAlgorithm(vertx);
        vertx.close();
        Async async = context.async();
        algorithm.deployRetryVerticle()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals("Vert.x closed", cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    private Gossip createGossipAlgorithm(final Vertx vertx) {
        return new Gossip(new Controller(new Configuration()), new MessageStore(), vertx);
    }
}
