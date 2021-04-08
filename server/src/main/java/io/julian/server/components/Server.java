package io.julian.server.components;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.api.DistributedAlgorithmVerticle;
import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ErrorHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.endpoints.client.DeleteMessageHandler;
import io.julian.server.endpoints.client.GetMessageHandler;
import io.julian.server.endpoints.client.GetOverviewHandler;
import io.julian.server.endpoints.client.PostMessageHandler;
import io.julian.server.endpoints.control.GetServerSettingsHandler;
import io.julian.server.endpoints.coordination.CoordinationMessageHandler;
import io.julian.server.endpoints.coordination.GetLabelHandler;
import io.julian.server.endpoints.coordination.SetLabelHandler;
import io.julian.server.endpoints.control.SetServerSettingsHandler;
import io.julian.server.endpoints.gates.ProbabilisticFailureGate;
import io.julian.server.endpoints.gates.UnreachableGate;
import io.julian.server.models.DistributedAlgorithmSettings;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

@Getter
public class Server {
    private static final Logger log = LogManager.getLogger(Server.class);
    private OpenAPI3RouterFactory routerFactory;
    private final MessageStore messages;
    private final static String[] OPERATION_IDS = new String[]{"postMessage", "getMessage", "deleteMessage",
        "setServerSettings", "getServerSettings", "setLabel", "getLabel", "sendCoordinationMessage", "getOverview"};
    private final Configuration configuration = new Configuration();
    private final Controller controller = new Controller(configuration);
    // Will not be null as startServer should be called after loading deployDistributedAlgorithmVerticle
    private DistributedAlgorithmVerticle verticle;

    public Server() {
        this.messages = new MessageStore();
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
        log.info("Adding handlers");
        // Client Endpoints
        PostMessageHandler postMessageHandler = new PostMessageHandler();
        GetMessageHandler getMessageHandler = new GetMessageHandler();
        DeleteMessageHandler deleteMessageHandler = new DeleteMessageHandler();
        GetOverviewHandler overviewHandler = new GetOverviewHandler();

        // Coordination Endpoints
        CoordinationMessageHandler coordinationMessageHandler = new CoordinationMessageHandler();
        SetLabelHandler setLabelHandler = new SetLabelHandler();
        GetLabelHandler getLabelHandler = new GetLabelHandler();

        List<AbstractServerHandler> handlers = Arrays.asList(postMessageHandler, getMessageHandler, deleteMessageHandler,
            coordinationMessageHandler, setLabelHandler, getLabelHandler);
        // Gate Handlers
        ProbabilisticFailureGate probabilisticFailureGate = new ProbabilisticFailureGate();
        UnreachableGate unreachableGate = new UnreachableGate();
        registerGates(handlers, probabilisticFailureGate, unreachableGate);

        // Server Control Handlers
        SetServerSettingsHandler setServerSettingsHandler = new SetServerSettingsHandler();
        GetServerSettingsHandler getServerSettingsHandler = new GetServerSettingsHandler();

        ServerComponents components = createServerComponents(vertx);

        routerFactory.addHandlerByOperationId("postMessage", routingContext -> postMessageHandler.runThroughHandlers(routingContext, components));
        routerFactory.addHandlerByOperationId("getMessage", routingContext -> getMessageHandler.runThroughHandlers(routingContext, components));
        routerFactory.addHandlerByOperationId("deleteMessage", routingContext -> deleteMessageHandler.runThroughHandlers(routingContext, components));
        routerFactory.addHandlerByOperationId("getOverview", routingContext -> overviewHandler.runThroughHandlers(routingContext, components));

        routerFactory.addHandlerByOperationId("setLabel", routingContext -> setLabelHandler.runThroughHandlers(routingContext, components));
        routerFactory.addHandlerByOperationId("getLabel", routingContext -> getLabelHandler.runThroughHandlers(routingContext, components));
        routerFactory.addHandlerByOperationId("sendCoordinationMessage",
            routingContext -> coordinationMessageHandler.runThroughHandlers(routingContext, components));

        routerFactory.addHandlerByOperationId("setServerSettings", routingContext -> setServerSettingsHandler.handle(routingContext, controller));
        routerFactory.addHandlerByOperationId("getServerSettings", routingContext -> getServerSettingsHandler.handle(routingContext, controller));

        addFailureHandlers();
        log.traceExit();
    }

    private void addFailureHandlers() {
        log.traceEntry();
        ErrorHandler errorHandler = new ErrorHandler();
        log.info("Loading failure handlers");
        for (String operationId : OPERATION_IDS) {
            log.trace(String.format("Adding failure handler for operation '%s'", operationId));
            routerFactory.addFailureHandlerByOperationId(operationId, errorHandler::handle);
        }
        log.info("Finished loading failure handlers");
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
            T algorithm = loader.loadJar(settings.getJarPath(), settings.getPackageName(), controller, messages, vertx);
            verticle = new DistributedAlgorithmVerticle(algorithm, vertx);
            log.info("Successfully deployed distributed algorithm into vertx");
            return deployHelper(verticle, vertx);
        } catch (Exception e) {
            log.info("Failed to deploy distributed algorithm");
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

    // Exposed For Testing
    public ServerComponents createServerComponents(final Vertx vertx) {
        log.traceEntry();
        return log.traceExit(new ServerComponents(messages, controller, vertx, verticle));
    }

    private void registerGates(final List<AbstractServerHandler> handlers,
                               final ProbabilisticFailureGate probabilisticFailureGate,
                               final UnreachableGate unreachableGate) {
        log.traceEntry(() -> handlers, () -> probabilisticFailureGate, () -> unreachableGate);
        log.info(String.format("Registering '%s' and '%s' handler gates",
            probabilisticFailureGate.getClass().getSimpleName(),
            unreachableGate.getClass().getSimpleName()));
        handlers.forEach(handler -> handler.registerGates(probabilisticFailureGate, unreachableGate));
        log.info("Finished registering handler gates");
        log.traceExit();
    }
}
