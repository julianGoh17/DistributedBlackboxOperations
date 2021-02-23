package handlers;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LeadershipElectionHandler {
    private final Logger logger = LogManager.getLogger(LeadershipElectionHandler.class);

    public final static String LEADER_LABEL = "leader";
    public final static String FOLLOWER_LABEL = "leader";

    public boolean doesExistLeaderInServers(final RegistryManager manager) {
        logger.traceEntry(() -> manager);
        return logger.traceExit(manager.getOtherServersWithLabel(LEADER_LABEL).size() > 0);
    }

    public void leadershipElection(final RegistryManager manager, final ServerClient client) {
        logger.traceEntry(() -> manager, () -> client);
        logger.traceExit();
    }
}
