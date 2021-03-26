package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.models.CandidateInformation;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Getter
public class LeadershipElectionHandler {
    private final Logger log = LogManager.getLogger(LeadershipElectionHandler.class);
    public final static String LEADER_LABEL = "leader";
    public final static String FOLLOWER_LABEL = "follower";
    public final static String TYPE = "candidate_information";

    private final long candidateNumber;
    private final CandidateInformationRegistry candidateRegistry;
    private final ServerClient client;
    private final RegistryManager registryManager;
    private final Controller controller;
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages;

    public LeadershipElectionHandler(final long candidateNumber, final CandidateInformationRegistry candidateRegistry, final ServerClient client, final RegistryManager registryManager, final Controller controller, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        this.candidateNumber = candidateNumber;
        this.candidateRegistry = candidateRegistry;
        this.client = client;
        this.registryManager = registryManager;
        this.controller = controller;
        this.deadCoordinationMessages = deadCoordinationMessages;
    }

    /**
     * Adds information about another server's candidate information (port, host and candidate number) to the server's candidate registry
     * @param information another servers candidate information
     */
    public void addCandidateInformation(final CandidateInformation information) {
        log.traceEntry(() -> information);
        candidateRegistry.addCandidateInformation(information);
        log.traceExit();
    }

    /**
     * Broadcast the server's candidate number to all other servers
     * @return A composite future that contains the outcome of all individual broadcasts to all other servers
     */
    public Future<Void> broadcast() {
        log.traceEntry();
        final ServerConfiguration currentConfig = controller.getServerConfiguration();
        log.info(String.format("Broadcasting candidate number '%d' to all servers", candidateNumber));
        Promise<Void> res = Promise.promise();
        List<Future> sentRequests = registryManager.getOtherServers()
            .stream()
            .map(config -> {
                log.info(String.format("Broadcasting server's candidate information to '%s:%d'", config.getHost(), config.getPort()));
                return client.sendCoordinateMessageToServer(config, createCandidateInformationMessage(candidateNumber, currentConfig));
            })
            .collect(Collectors.toList());

        CompositeFuture.all(sentRequests)
            .onSuccess(v -> res.complete())
            .onFailure(cause -> {
                log.info(String.format("Failed to broadcast candidate number '%d' to all servers", candidateNumber));
                deadCoordinationMessages.add(createCandidateInformationMessage(candidateNumber, currentConfig));
                res.fail(cause);
            });

        return log.traceExit(res.future());
    }

    /**
     * Exposed for testing
     * Creates a coordinate message with the candidate information stored inside the user definition
     * @param candidateNumber candidate number to send to other servers
     * @param serverConfig the server's host and port
     * @return a message to be send to other servers
     */
    public CoordinationMessage createCandidateInformationMessage(final long candidateNumber, final ServerConfiguration serverConfig) {
        log.traceEntry(() -> candidateNumber, () -> serverConfig);
        return log.traceExit(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN, null, TYPE),
            null,
            new CandidateInformation(serverConfig.getHost(), serverConfig.getPort(), candidateNumber).toJson()));
    }

    public ConcurrentLinkedQueue<CoordinationMessage> getDeadCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(deadCoordinationMessages);
    }

    /**
     * Checks to see if the registry is filled with the candidate information of the other servers
     * @param manager manager containing the address of all other servers
     * @return a boolean whether or not the registry is filled and thus, can update leader
     */
    public boolean canUpdateLeader(final RegistryManager manager) {
        log.traceEntry(() -> manager);
        return log.traceExit(candidateRegistry.isRegistryFilled(manager));
    }

    /**
     * Update to the server with the next greater candidate number than the current candidate number. It will then correctly
     * update the label of each server in the registry manager depending on who has become the leader.
     * @param manager manager containing the address of all other servers
     * @param controller server controller
     */
    public void updateLeader(final RegistryManager manager, final Controller controller) {
        log.traceEntry(() -> manager, () -> controller);
        candidateRegistry.updateNextLeader();
        ServerConfiguration leaderConfig = candidateRegistry.getLeaderServerConfiguration();
        if (controller.getServerConfiguration().isHostAndPortEqual(leaderConfig)) {
            log.info(String.format("Server '%s:%d' has been promoted to '%s'",
                controller.getServerConfiguration().getHost(), controller.getServerConfiguration().getPort(), LEADER_LABEL));
            controller.setLabel(LEADER_LABEL);
            manager.getOtherServers().forEach(serverConfiguration -> {
                log.info(String.format("Updating server registry of '%s:%d' to '%s'", serverConfiguration.getHost(), serverConfiguration.getPort(), FOLLOWER_LABEL));
                serverConfiguration.setLabel(FOLLOWER_LABEL);
            });
        } else {
            log.info(String.format("Server '%s:%d' is the '%s', updating internal registry to set other servers to respective label",
                controller.getServerConfiguration().getHost(), controller.getServerConfiguration().getPort(), FOLLOWER_LABEL));
            controller.setLabel(FOLLOWER_LABEL);
            manager.getOtherServers()
                .forEach(config -> {
                    if (config.isHostAndPortEqual(leaderConfig)) {
                        log.info(String.format("Updating server registry of '%s:%d' to '%s'", config.getHost(), config.getPort(), LEADER_LABEL));
                        config.setLabel(LEADER_LABEL);
                    } else {
                        log.info(String.format("Updating server registry of '%s:%d' to '%s'", config.getHost(), config.getPort(), FOLLOWER_LABEL));
                        config.setLabel(FOLLOWER_LABEL);
                    }
                });
        }
        log.traceExit();
    }
}
