package io.julian.gossip.components;

import org.junit.Assert;
import org.junit.Test;

public class GossipConfigurationTest {
    @Test
    public void TestInit() {
        GossipConfiguration configuration = new GossipConfiguration();
        Assert.assertEquals(GossipConfiguration.DEFAULT_INACTIVE_PROBABILITY, configuration.getInactiveProbability(), 0);
    }

    @Test
    public void TestSetter() {
        GossipConfiguration configuration = new GossipConfiguration();
        float newProbability = 1;
        configuration.setInactiveProbability(newProbability);
        Assert.assertEquals(newProbability, configuration.getInactiveProbability(), 0);
    }
}
