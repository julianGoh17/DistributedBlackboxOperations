package io.julian.metrics.collector;

import io.julian.metrics.collector.server.Configuration;
import io.julian.metrics.collector.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private final static Logger log = LogManager.getLogger(Main.class);

    public static void main(final String[] args) {
        log.traceEntry();
        Configuration configuration = new Configuration();
        Vertx vertx = Vertx.vertx();
        Server server = new Server(configuration);
        HttpServer api = vertx.createHttpServer(new HttpServerOptions()
            .setPort(configuration.getServerPort())
            .setHost(configuration.getServerHost()));

        server.startServer(vertx, configuration.getOpenApiSpecLocation())
            .onSuccess(v -> {
                log.info(String.format("Successfully started server at %s:%d", configuration.getServerHost(), configuration.getServerPort()));
                api.requestHandler(server.getRouterFactory().getRouter()).listen();
            })
            .onFailure(cause -> {
                log.error(cause);
                vertx.close();
            });
    }
}
