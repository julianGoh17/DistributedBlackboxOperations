package io.julian.gossip.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class GossipConfiguration {
    private final static Logger log = LogManager.getLogger(GossipConfiguration.class);
    public final static String INACTIVE_PROBABILITY_ENV = "INACTIVE_PROBABILITY";
    public final static float DEFAULT_INACTIVE_PROBABILITY = 0.4f;
    private float inactiveProbability;

    public GossipConfiguration() {
        this.inactiveProbability = getOrDefault(INACTIVE_PROBABILITY_ENV, DEFAULT_INACTIVE_PROBABILITY);
    }

    public float getInactiveProbability() {
        log.traceEntry();
        return log.traceExit(inactiveProbability);
    }

    public void setInactiveProbability(final float inactiveProbability) {
        log.traceEntry(() -> inactiveProbability);
        this.inactiveProbability = inactiveProbability;
        log.traceExit();
    }

    public static float getOrDefault(final String key, final float defaultVal) {
        log.traceEntry(() -> key, () -> defaultVal);

        try {
            return log.traceExit(Optional.ofNullable(System.getenv(key)).map(Float::parseFloat).orElse(defaultVal));
        } catch (NumberFormatException e) {
            return log.traceExit(defaultVal);
        }
    }
}
