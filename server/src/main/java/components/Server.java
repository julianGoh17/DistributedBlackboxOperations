package components;

import endpoints.ErrorHandler;
import endpoints.PostMessageHandler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class Server {
    private static final Logger logger = LogManager.getLogger(Server.class);
    private OpenAPI3RouterFactory routerFactory;
    private final MessageStore messages;
    private final String[] operationIds = new String[]{"postMessage"};

    public static final int DEFAULT_SERVER_PORT = 8888;
    public static final String DEFAULT_HOST = "localhost";
    public static final String OPENAPI_SPEC_LOCATION = "src/main/resources/ServerEndpoints.yaml";

    public Server() {
        this.messages = new MessageStore();
    }

    public Promise<Boolean> startServer(Vertx vertx, String specLocation) {
        logger.traceEntry(() -> vertx);
        Promise<Boolean> hasDeployed = Promise.promise();
        OpenAPI3RouterFactory.create(vertx, specLocation, ar -> {
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
        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.handle(routingContext, messages));

        addFailureHandlers();
        logger.traceExit();
    }

    private void addFailureHandlers() {
        logger.traceEntry();
        ErrorHandler errorHandler = new ErrorHandler();
        for (String operationId : operationIds) {
            logger.trace(String.format("Adding failure handler for operation '%s'", operationId));
            routerFactory.addFailureHandlerByOperationId(operationId, errorHandler::handle);
        }
        logger.traceExit();
    }
}
