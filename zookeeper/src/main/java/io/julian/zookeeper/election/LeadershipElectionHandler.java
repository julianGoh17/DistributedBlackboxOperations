package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
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

    public LeadershipElectionHandler() {
        candidateNumber = generateRandomNumberWithManyDigits();
        candidateRegistry = initializeCandidateInformationRegistry(Configuration.getServerHost(), Configuration.getServerPort(), candidateNumber);
        broadcastHandler = new BroadcastCandidateInformationHandler();
    }

    public void addCandidateInformation(final CandidateInformation information) {
        log.traceEntry(() -> information);
        candidateRegistry.addCandidateInformation(information);
        log.traceExit();
    }

    public CompositeFuture broadcast(final RegistryManager manager, final ServerClient client, final Controller controller) {
        log.traceEntry(() -> manager, () -> client, () -> controller);
        return log.traceExit(broadcastHandler.broadcast(manager, client, candidateNumber, controller.getServerConfiguration()));
    }

    public void updateLeader(final RegistryManager manager, final Controller controller) {
        log.traceEntry(() -> manager, () -> controller);
        candidateRegistry.updateNextLeader();
        ServerConfiguration leaderConfig = candidateRegistry.getLeaderServerConfiguration();
        if (leaderConfig.isEqual(controller.getServerConfiguration())) {
            log.info(String.format("Server has been promoted to '%s'", LEADER_LABEL));
            controller.setLabel(LEADER_LABEL);
            manager.getOtherServers().forEach(serverConfiguration -> {
                log.info(String.format("Updating server registry of '%s:%d' to '%s'", serverConfiguration.getHost(), serverConfiguration.getPort(), FOLLOWER_LABEL));
                serverConfiguration.setLabel(FOLLOWER_LABEL);
            });
        } else {
            log.info(String.format("Server is the '%s', updating internal registry to set other servers to respective label", FOLLOWER_LABEL));
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

    /**
     * Exposed For Testing
     * Generates a random number with many digits which will be used to determine the leadership of a server
     * @return many digit number
     */
    public long generateRandomNumberWithManyDigits() {
        log.traceEntry();
        return log.traceExit((long) (Math.random() * Math.pow(10, 10)));
    }

    public CandidateInformationRegistry initializeCandidateInformationRegistry(final String host, final int port, final long candidateNumber) {
        log.traceEntry(() -> candidateNumber);
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(host, port, candidateNumber));
        return log.traceExit(registry);
    }
}
