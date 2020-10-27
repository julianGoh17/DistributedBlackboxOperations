package components;

import endpoints.ErrorHandler;
import endpoints.GetMessageHandler;
import endpoints.PostMessageHandler;
import endpoints.PutMessageHandler;
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
    private final static String[] OPERATION_IDS = new String[]{"postMessage", "getMessage", "putMessage"};

    public static final int DEFAULT_SERVER_PORT = 8888;
    public static final String DEFAULT_HOST = "localhost";
    public static final String OPENAPI_SPEC_LOCATION = "src/main/resources/ServerEndpoints.yaml";

    public Server() {
        this.messages = new MessageStore();
    }

    public Promise<Boolean> startServer(Vertx vertx, String specLocation) {
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
        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("getMessage", routingContext -> getMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("putMessage", routingContext -> putMessageHandler.handle(routingContext, messages));
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
