package io.julian.server;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

public class Main {
    public static void main(final String[] args) {
        Vertx vertx = Vertx.vertx();
        Server server = new Server();
        Promise<Boolean> hasDeployed = server.startServer(vertx, Configuration.getOpenapiSpecLocation());

        hasDeployed.future().onSuccess(res -> {
            HttpServer api = vertx.createHttpServer(new HttpServerOptions()
                .setPort(Configuration.getServerPort())
                .setHost(Configuration.getServerHost()));
            api.requestHandler(server.getRouterFactory().getRouter()).listen();
        });
    }

    private static <T extends AbstractVerticle> Future<T> deployHelper(final T verticle, final Vertx vertx) {
        Promise<T> deployment = Promise.promise();
        vertx.deployVerticle(verticle, result -> {
            if (result.succeeded()) {
                deployment.complete();
            } else {
                deployment.fail("Could not deploy");
            }
        });
        return deployment.future();
    }
}
