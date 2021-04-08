package io.julian.server.endpoints.client;

import io.julian.server.endpoints.AbstractServerHandler;
import io.julian.server.endpoints.ServerComponents;
import io.julian.server.models.response.ServerOverview;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GetOverviewHandler extends AbstractServerHandler {
    private final static Logger log = LogManager.getLogger(GetOverviewHandler.class);

    public void handle(final RoutingContext context, final ServerComponents components) {
        log.traceEntry(() -> context, () -> components);
        log.info(String.format("%s attempting to get overview of server", GetOverviewHandler.class.getSimpleName()));

        sendResponseBack(context,
            200,
            new ServerOverview(components.controller.getConfiguration().getServerHost(),
                components.controller.getConfiguration().getServerPort(),
                components.messageStore.getNumberOfMessages()
                ).toJson());

        log.info(String.format("%s successfully got overview of server", GetOverviewHandler.class.getSimpleName()));
        log.traceExit();
    }
}
