package io.julian.zookeeper.models;

import io.julian.zookeeper.discovery.DiscoveryHandler;
import io.julian.zookeeper.discovery.LeaderDiscoveryHandler;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.synchronize.SynchronizeHandler;
import io.julian.zookeeper.write.FollowerWriteHandler;
import io.julian.zookeeper.write.LeaderWriteHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public enum Stage {
    SETUP,
    DISCOVERY,
    SYNCHRONIZE,
    WRITE,
    START;

    private static final Logger log = LogManager.getLogger(Stage.class);
    private static final String EMPTY_STRING = "";

    public static Stage fromType(final String type) {
        log.traceEntry(() -> type);

        switch (Optional.ofNullable(type).orElse(EMPTY_STRING)) {
            case LeadershipElectionHandler.TYPE:
                return log.traceExit(SETUP);
            case DiscoveryHandler.DISCOVERY_TYPE:
            case LeaderDiscoveryHandler.LEADER_STATE_UPDATE_TYPE:
                return log.traceExit(DISCOVERY);
            case SynchronizeHandler.SYNCHRONIZE_TYPE:
                return log.traceExit(SYNCHRONIZE);
            case LeaderWriteHandler.TYPE:
            case FollowerWriteHandler.ACK_TYPE:
            case FollowerWriteHandler.FORWARD_TYPE:
                return log.traceExit(WRITE);
            default:
                return log.traceExit(START);
        }
    }

    public String toString() {
        log.traceExit();
        switch (this) {
            case SETUP:
                return "Set Up";
            case DISCOVERY:
                return "Discovery";
            case SYNCHRONIZE:
                return "Synchronize";
            case WRITE:
                return "Write";
            default:
                return "Start";
        }
    }
}
