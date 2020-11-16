package io.julian.server;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import static io.julian.server.components.Server.DEFAULT_HOST;
import static io.julian.server.components.Server.DEFAULT_SERVER_PORT;

public class Main {
    public static void main(final String[] args) {
        Vertx vertx = Vertx.vertx();
        Server server = new Server();
        Promise<Boolean> hasDeployed = server.startServer(vertx, Configuration.getOpenapiSpecLocation());

        hasDeployed.future().onSuccess(res -> {
            HttpServer api = vertx.createHttpServer(new HttpServerOptions()
                .setPort(DEFAULT_SERVER_PORT)
                .setHost(DEFAULT_HOST));
            api.requestHandler(server.getRouterFactory().getRouter()).listen();
        });
    }
}
