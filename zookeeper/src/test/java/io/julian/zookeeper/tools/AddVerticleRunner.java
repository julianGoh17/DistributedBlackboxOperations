package io.julian.zookeeper.tools;

import io.julian.zookeeper.controller.State;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddVerticleRunner {
    public final static int VERTICLES = 3;

    private final Vertx vertx;
    private final List<AddVerticle> verticles = new ArrayList<>();

    public AddVerticleRunner(final Vertx vertx) {
        this.vertx = vertx;
    }

    public void runVerticles() {
        verticles.forEach(verticle -> vertx.eventBus().publish(verticle.formatAddress(), ""));
    }

    public void deployTestVerticles(final TestContext context, final State state) {
        for (int num = 0; num < VERTICLES; num++) {
            verticles.add(new AddVerticle(state, num));
        }

        List<Future> futures = verticles.stream().map(this::deployHelper).collect(Collectors.toList());
        Async async = context.async();
        CompositeFuture.all(futures)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
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
}
