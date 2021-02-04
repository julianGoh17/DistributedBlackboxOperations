package io.julian.server.components;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.coordination.CoordinationMessageHandler;
import io.julian.server.endpoints.ErrorHandler;
import io.julian.server.endpoints.client.GetMessageHandler;
import io.julian.server.endpoints.coordination.LabelHandler;
import io.julian.server.endpoints.coordination.SetStatusHandler;
import io.julian.server.endpoints.client.PostMessageHandler;
import io.julian.server.endpoints.client.PutMessageHandler;
import io.julian.server.models.DistributedAlgorithmSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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
    private final static String[] OPERATION_IDS = new String[]{"postMessage", "getMessage", "putMessage", "setStatus", "setLabel", "sendCoordinationMessage"};
    private final Controller controller = new Controller();

    public Server() {
        this.messages = new MessageStore();
    }

    public Promise<Boolean> startServer(final Vertx vertx, final String specLocation) {
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
        return hasDeployed;
    }

    public void addHandlers(final Vertx vertx) {
        log.traceEntry(() -> vertx);
        log.info("Adding handlers");
        PostMessageHandler postMessageHandler = new PostMessageHandler();
        GetMessageHandler getMessageHandler = new GetMessageHandler();
        PutMessageHandler putMessageHandler = new PutMessageHandler();
        SetStatusHandler setStatusHandler = new SetStatusHandler();
        CoordinationMessageHandler coordinationMessageHandler = new CoordinationMessageHandler();
        LabelHandler labelHandler = new LabelHandler();
        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("getMessage", routingContext -> getMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("putMessage", routingContext -> putMessageHandler.handle(routingContext, messages));
        routerFactory.addHandlerByOperationId("setStatus", routingContext -> setStatusHandler.handle(routingContext, controller));
        routerFactory.addHandlerByOperationId("setLabel", routingContext -> labelHandler.handle(routingContext, controller));
        routerFactory.addHandlerByOperationId("sendCoordinationMessage", routingContext -> coordinationMessageHandler.handle(routingContext, controller, vertx));
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

    public <T extends DistributedAlgorithm> Future<String> deployDistributedAlgorithmVerticle(final Controller controller, final Vertx vertx, final DistributedAlgorithmSettings settings) {
        log.traceEntry(() -> controller, () -> vertx);
        if (!settings.isJarFilePathEnvInstantiated() || !settings.isPackageNameEnvInstantiated()) {
            String skipLog = String.format("Skipping loading distributed algorithm because environmental variable '%s' or '%s' not instantiated", Configuration.JAR_FILE_PATH_ENV, Configuration.PACKAGE_NAME_ENV);
            log.info(skipLog);
            return Future.succeededFuture(skipLog);
        }

        log.info("Loading distributed algorithm");
        ClassLoader loader = new ClassLoader();
        try {
            T algorithm = loader.loadJar(settings.getJarPath(), settings.getPackageName(), controller);
            DistributedAlgorithmVerticle verticle = new DistributedAlgorithmVerticle(algorithm);
            return deployHelper(verticle, vertx);
        } catch (Exception e) {
            log.error(e);
            return Future.failedFuture(e);
        }
    }

    private <T extends AbstractVerticle> Future<String> deployHelper(final T verticle, final Vertx vertx) {
        log.traceEntry(() -> verticle, () -> verticle);
        Promise<String> deployment = Promise.promise();
        vertx.deployVerticle(verticle, res -> {
            if (res.succeeded()) {
                deployment.complete(res.result());
            } else {
                deployment.fail("Could not deploy");
            }
        });
        return log.traceExit(deployment.future());
    }
}
