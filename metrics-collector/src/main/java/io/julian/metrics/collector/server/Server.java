package io.julian.metrics.collector.server;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class Server {
    private final static Logger log = LogManager.getLogger(Server.class.getName());
    private OpenAPI3RouterFactory routerFactory;

    public final static String DEFAULT_OPENAPI_SPEC_LOCATION = "src/main/resources/metrics-collector-endpoints.yaml";

    public Future<Boolean> startServer(final Vertx vertx, final String specLocation) {
        log.traceEntry(() -> vertx, () -> specLocation);
        Promise<Boolean> hasDeployed = Promise.promise();
        OpenAPI3RouterFactory.create(vertx, specLocation, ar -> {
            if (ar.succeeded()) {
                log.info("Successfully created server");
                routerFactory = ar.result();
                addHandlers(vertx);
                hasDeployed.complete(true);
            } else {
                Throwable exception = ar.cause();
                log.error(exception);
                hasDeployed.fail(exception);
            }
        });
        return log.traceExit(hasDeployed.future());
    }

    public void addHandlers(final Vertx vertx) {
        log.traceEntry(() -> vertx);

        log.traceExit();
    }
}
