package io.julian.server.components;

import io.julian.server.endpoints.ErrorHandler;
import io.julian.server.endpoints.GetMessageHandler;
import io.julian.server.endpoints.LabelHandler;
import io.julian.server.endpoints.SetStatusHandler;
import io.julian.server.endpoints.PostMessageHandler;
import io.julian.server.endpoints.PutMessageHandler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private OpenAPI3RouterFactory routerFactory;
    private final MessageStore messages;
    private final static String[] OPERATION_IDS = new String[]{"postMessage", "getMessage", "putMessage", "setStatus", "setLabel"};
    private final Controller controller = new Controller();

    public Server() {
        this.messages = new MessageStore();
    }

    public Promise<Boolean> startServer(final Vertx vertx, final String specLocation) {
        log.traceEntry(() -> vertx);
        Promise<Boolean> hasDeployed = Promise.promise();
        OpenAPI3RouterFactory.create(vertx, specLocation, ar -> {
            if (ar.succeeded()) {
                log.info("Successfully created server");
                routerFactory = ar.result();
                addHandlers();
                hasDeployed.complete(true);
            } else {
                Throwable exception = ar.cause();
                log.error(exception);
                hasDeployed.fail(exception);
            }
        });
        return hasDeployed;
    }

    public void addHandlers() {
        log.traceEntry();
        log.info("Adding handlers");
        PostMessageHandler postMessageHandler = new PostMessageHandler();
        GetMessageHandler getMessageHandler = new GetMessageHandler();
        PutMessageHandler putMessageHandler = new PutMessageHandler();
        SetStatusHandler setStatusHandler = new SetStatusHandler();
        LabelHandler labelHandler = new LabelHandler();
        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("getMessage", routingContext -> getMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("putMessage", routingContext -> putMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("setStatus", routingContext -> setStatusHandler.handle(routingContext, controller));
        routerFactory.addHandlerByOperationId("setLabel", routingContext -> labelHandler.handle(routingContext, controller));
        addFailureHandlers();
        log.traceExit();
    }

    private void addFailureHandlers() {
        log.traceEntry();
        ErrorHandler errorHandler = new ErrorHandler();
        for (String operationId : OPERATION_IDS) {
            log.trace(String.format("Adding failure handler for operation '%s'", operationId));
            routerFactory.addFailureHandlerByOperationId(operationId, errorHandler::handle);
        }
        log.traceExit();
    }
}
