package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Controller;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.models.CandidateInformation;
import io.vertx.core.CompositeFuture;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class LeadershipElectionHandler {
    private final Logger log = LogManager.getLogger(LeadershipElectionHandler.class);
    public final static String LEADER_LABEL = "leader";
    public final static String FOLLOWER_LABEL = "follower";

    private final long candidateNumber;
    private final CandidateInformationRegistry candidateRegistry;
    private final BroadcastCandidateInformationHandler broadcastHandler;

    public LeadershipElectionHandler(final long candidateNumber, final CandidateInformationRegistry candidateRegistry) {
        this.candidateNumber = candidateNumber;
        this.candidateRegistry = candidateRegistry;
        broadcastHandler = new BroadcastCandidateInformationHandler();
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
     * @param manager manager containing the address of all other servers
     * @param client the API client which knows how to talk to the other servers
     * @param controller the current server controller
     * @return A composite future that contains the outcome of all individual broadcasts to all other servers
     */
    public CompositeFuture broadcast(final RegistryManager manager, final ServerClient client, final Controller controller) {
        log.traceEntry(() -> manager, () -> client, () -> controller);
        return log.traceExit(broadcastHandler.broadcast(manager, client, candidateNumber, controller.getServerConfiguration()));
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
