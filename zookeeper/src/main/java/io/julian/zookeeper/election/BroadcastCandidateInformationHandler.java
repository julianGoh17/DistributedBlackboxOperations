package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.julian.zookeeper.models.CandidateInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class BroadcastCandidateInformationHandler {
    private final Logger logger = LogManager.getLogger(BroadcastCandidateInformationHandler.class);

    public CompositeFuture broadcast(final RegistryManager manager, final ServerClient client,
                          final long candidateNumber, final ServerConfiguration currentConfig) {
        logger.traceEntry(() -> manager, () -> client);
        List<Future> sentRequests = manager.getOtherServers()
            .stream()
            .map(config -> client.sendCoordinateMessageToServer(config, createCandidateInformationMessage(candidateNumber, currentConfig)))
            .collect(Collectors.toList());
        return logger.traceExit(CompositeFuture.all(sentRequests));
    }

    /**
     * Exposed for testing
     */
    public CoordinationMessage createCandidateInformationMessage(final long candidateNumber, final ServerConfiguration serverConfig) {
        logger.traceEntry(() -> candidateNumber, () -> serverConfig);
        return logger.traceExit(new CoordinationMessage(HTTPRequest.UNKNOWN,
            new CandidateInformation(serverConfig.getHost(), serverConfig.getPort(), candidateNumber).toJson()));
    }
}
