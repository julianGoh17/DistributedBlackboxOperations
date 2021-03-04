package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.models.CandidateInformation;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class BroadcastCandidateInformationHandler {
    private final Logger logger = LogManager.getLogger(BroadcastCandidateInformationHandler.class);
    public final static String TYPE = "candidate_information";

    /**
     * Broadcast the server's candidate number to all other servers
     * @param manager manager containing the address of all other servers
     * @param client the API client which knows how to talk to the other servers
     * @param candidateNumber the server's candidate number
     * @param currentConfig the current server's configuration
     * @return A composite future that contains the outcome of all individual broadcasts to all other servers
     */
    public CompositeFuture broadcast(final RegistryManager manager, final ServerClient client,
                          final long candidateNumber, final ServerConfiguration currentConfig) {
        logger.traceEntry(() -> manager, () -> client);
        logger.info(String.format("Broadcasting candidate number '%d' to all servers", candidateNumber));
        List<Future> sentRequests = manager.getOtherServers()
            .stream()
            .map(config -> {
                logger.info(String.format("Broadcasting server's candidate information to '%s:%d'", currentConfig.getHost(), currentConfig.getPort()));
                return client.sendCoordinateMessageToServer(config, createCandidateInformationMessage(candidateNumber, currentConfig));
            })
            .collect(Collectors.toList());
        return logger.traceExit(CompositeFuture.all(sentRequests));
    }

    /**
     * Exposed for testing
     * Creates a coordinate message with the candidate information stored inside the user definition
     * @param candidateNumber candidate number to send to other servers
     * @param serverConfig the server's host and port
     * @return a message to be send to other servers
     */
    public CoordinationMessage createCandidateInformationMessage(final long candidateNumber, final ServerConfiguration serverConfig) {
        logger.traceEntry(() -> candidateNumber, () -> serverConfig);
        return logger.traceExit(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN, TYPE),
            null,
            new CandidateInformation(serverConfig.getHost(), serverConfig.getPort(), candidateNumber).toJson()));
    }
}
