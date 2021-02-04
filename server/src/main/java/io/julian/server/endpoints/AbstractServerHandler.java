package io.julian.server.endpoints;

import io.julian.server.components.Controller;
import io.julian.server.endpoints.gates.HandlerGate;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractServerHandler {
    private final static Logger log = LogManager.getLogger(AbstractServerHandler.class.getName());
    private final List<HandlerGate> registeredGates = new ArrayList<>();

    public void registerGates(final HandlerGate... gates) {
        log.traceEntry(() -> gates);
        registeredGates.addAll(Arrays.asList(gates));
        log.traceExit();
    }

    protected Future<String> runThroughGates(final RoutingContext context, final Controller controller) {
        log.traceEntry(() -> context, () -> controller);
        List<Future> gates = registeredGates.stream()
            .map(gate -> gate.handle(context, controller))
            .collect(Collectors.toList());

        CompositeFuture compositeFuture = CompositeFuture.all(gates);
        Promise<String> succeededThroughGates = Promise.promise();

        compositeFuture
            .onSuccess(v -> succeededThroughGates.complete())
            .onFailure(succeededThroughGates::fail);

        return log.traceExit(succeededThroughGates.future());
    }

    protected abstract Future<String> handle(final RoutingContext context, final ServerComponents components);

    public void runThroughHandlers(final RoutingContext context, final ServerComponents components) {
        log.traceEntry();
        runThroughGates(context, components.controller)
            .onSuccess(v -> handle(context, components));
        log.traceExit();
    }
}
