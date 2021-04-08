package io.julian.metrics.collector.server;

import io.julian.metrics.collector.server.handlers.ErrorHandler;
import io.julian.metrics.collector.server.handlers.ReportHandler;
import io.julian.metrics.collector.server.handlers.TrackHandler;
import io.julian.metrics.collector.tracking.StatusTracker;
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
    private final StatusTracker tracker = new StatusTracker();
    private final Configuration configuration;

    public Server(final Configuration configuration) {
        this.configuration = configuration;
    }

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
        TrackHandler trackHandler = new TrackHandler(tracker);
        ReportHandler reportHandler = new ReportHandler(tracker, vertx, configuration);
        ErrorHandler errorHandler = new ErrorHandler();

        routerFactory.addHandlerByOperationId("trackMessage", trackHandler::handle);
        routerFactory.addFailureHandlerByOperationId("trackMessage", errorHandler::handle);
        routerFactory.addHandlerByOperationId("createReport", reportHandler::handle);
        routerFactory.addFailureHandlerByOperationId("createReport", errorHandler::handle);
        log.traceExit();
    }

    /*
     * Exposed for Testing
     */
    public StatusTracker getTracker() {
        log.traceEntry();
        return log.traceExit(tracker);
    }
}
