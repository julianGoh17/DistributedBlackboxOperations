package io.julian;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ZookeeperAlgorithmTest {
    @Test
    public void TestDeployVerticleSucceeds(final TestContext context) {
        Vertx vertx = Vertx.vertx();
        ZookeeperAlgorithm algorithm = new ZookeeperAlgorithm(vertx);

        Async async = context.async();
        algorithm.deployRetryVerticle()
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        vertx.close();
    }

    @Test
    public void TestDeployVerticle(final TestContext context) {
        Vertx vertx = Vertx.vertx();
        ZookeeperAlgorithm algorithm = new ZookeeperAlgorithm(vertx);
        vertx.close();
        Async async = context.async();
        algorithm.deployRetryVerticle()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals("Vert.x closed", cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }
}
