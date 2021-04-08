package io.julian.server;

import io.julian.server.components.Server;
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

        server.deployDistributedAlgorithmVerticle(server.getController(), vertx, server.getConfiguration().getDistributedAlgorithmSettings())
            .compose(v -> server.startServer(vertx, server.getConfiguration().getOpenapiSpecLocation()))
            .onSuccess(v -> {
                HttpServer api = vertx.createHttpServer(new HttpServerOptions()
                    .setPort(server.getConfiguration().getServerPort())
                    .setHost(server.getConfiguration().getServerHost()));
                api.requestHandler(server.getRouterFactory().getRouter()).listen();
                log.info(String.format("Successfully started server at %s:%d", server.getConfiguration().getServerHost(), server.getConfiguration().getServerPort()));
            })
            .onFailure(throwable -> {
                log.error(throwable);
                vertx.close();
            });
        log.traceExit();
    }
}
