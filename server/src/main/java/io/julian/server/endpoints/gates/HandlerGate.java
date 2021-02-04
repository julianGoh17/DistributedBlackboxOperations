package io.julian.server.endpoints.gates;

import io.julian.server.components.Controller;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public interface HandlerGate {
    Future<String> handle(final RoutingContext context, final Controller controller);
}
