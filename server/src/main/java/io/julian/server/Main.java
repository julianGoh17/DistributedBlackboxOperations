package io.julian.server;

import io.julian.server.components.Configuration;
import io.julian.server.components.Server;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class.getName());

    public static void main(final String[] args) {
        log.traceEntry(() -> args);
        Vertx vertx = Vertx.vertx();
        Server server = new Server();

        CompositeFuture.all(
            server.deployDistributedAlgorithmVerticle(server.getController(), vertx, Configuration.getDistributedAlgorithmSettings()),
            server.startServer(vertx, Configuration.getOpenapiSpecLocation()).future()
        )
            .onSuccess(v -> {
                HttpServer api = vertx.createHttpServer(new HttpServerOptions()
                    .setPort(Configuration.getServerPort())
                    .setHost(Configuration.getServerHost()));
                api.requestHandler(server.getRouterFactory().getRouter()).listen();
            })
            .onFailure(throwable -> {
                log.error(throwable);
                vertx.close();
            });
        log.traceExit();
    }
}
