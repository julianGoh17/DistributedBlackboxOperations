package io.julian.zookeeper.models;

import io.julian.zookeeper.discovery.DiscoveryHandler;
import io.julian.zookeeper.discovery.LeaderDiscoveryHandler;
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
    WRITE;

    private static final Logger log = LogManager.getLogger(Stage.class);
    private static final String EMPTY_STRING = "";

    public static Stage fromType(final String type) {
        log.traceEntry(() -> type);

        switch (Optional.ofNullable(type).orElse(EMPTY_STRING)) {
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
                return log.traceExit(SETUP);
        }
    }

    public String toString() {
        log.traceEntry();
        switch (this) {
            case SETUP:
                return log.traceExit("Set Up");
            case DISCOVERY:
                return log.traceExit("Discovery");
            case SYNCHRONIZE:
                return log.traceExit("Synchronize");
            case WRITE:
                return log.traceExit("Write");
            default:
                return log.traceExit("Start");
        }
    }

    public int toStageNumber() {
        log.traceEntry();
        switch (this) {
            case DISCOVERY:
                return log.traceExit(1);
            case SYNCHRONIZE:
                return log.traceExit(2);
            case WRITE:
                return log.traceExit(3);
            default:
                return log.traceExit(-1);
        }
    }
}
