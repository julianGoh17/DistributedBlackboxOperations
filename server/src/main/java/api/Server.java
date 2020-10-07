package api;

import endpoints.ErrorHandler;
import endpoints.PostMessageHandler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private OpenAPI3RouterFactory routerFactory;
    private final MessageStore messages;

    public static final int DEFAULT_SERVER_PORT = 8888;
    public static final String DEFAULT_HOST = "localhost";

    public Server() {
        this.messages = new MessageStore();
    }

    public Promise<Boolean> startServer(Vertx vertx) {
        logger.traceEntry(() -> vertx);
        Promise<Boolean> hasDeployed = Promise.promise();
        OpenAPI3RouterFactory.create(vertx, "src/main/resources/ServerEndpoints.yaml", ar -> {
            if (ar.succeeded()) {
                logger.info("Successfully created server");
                routerFactory = ar.result();
                addHandlers();
                hasDeployed.complete(true);
            } else {
                Throwable exception = ar.cause();
                logger.error(exception);
                hasDeployed.fail(exception);
            }
        });

        return hasDeployed;
    }

    public void addHandlers() {
        logger.traceEntry();
        logger.info("Adding handlers");
        PostMessageHandler postMessageHandler = new PostMessageHandler();
        ErrorHandler errorHandler = new ErrorHandler();
        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.handle(routingContext, messages));
        routerFactory.getRouter().errorHandler(404, errorHandler::handle);
        logger.traceExit();
    }

    public MessageStore getMessageStore() {
        logger.traceEntry();
        return logger.traceExit(messages);
    }

    public OpenAPI3RouterFactory getRouterFactory() {
        logger.traceEntry();
        return logger.traceExit(this.routerFactory);
    }
}
