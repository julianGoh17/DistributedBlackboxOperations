package io.julian.zookeeper.models;

import io.julian.zookeeper.discovery.DiscoveryHandler;
import io.julian.zookeeper.discovery.LeaderDiscoveryHandler;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.synchronize.SynchronizeHandler;
import io.julian.zookeeper.write.FollowerWriteHandler;
import io.julian.zookeeper.write.LeaderWriteHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class StageTest {
    @Test
    public void TestFromType() {
        HashMap<String, Stage> expectedMappings = new HashMap<>();
        expectedMappings.put(LeadershipElectionHandler.TYPE, Stage.SETUP);
        expectedMappings.put(DiscoveryHandler.DISCOVERY_TYPE, Stage.DISCOVERY);
        expectedMappings.put(LeaderDiscoveryHandler.LEADER_STATE_UPDATE_TYPE, Stage.DISCOVERY);
        expectedMappings.put(SynchronizeHandler.SYNCHRONIZE_TYPE, Stage.SYNCHRONIZE);
        expectedMappings.put(LeaderWriteHandler.TYPE, Stage.WRITE);
        expectedMappings.put(FollowerWriteHandler.ACK_TYPE, Stage.WRITE);
        expectedMappings.put(FollowerWriteHandler.FORWARD_TYPE, Stage.WRITE);

        for (final String key : expectedMappings.keySet()) {
            Assert.assertEquals(expectedMappings.get(key), Stage.fromType(key));
        }
    }

    @Test
    public void TestToStageNumber() {
        HashMap<Stage, Integer> expectedNumberings = new HashMap<>();
        expectedNumberings.put(Stage.SETUP, -1);
        expectedNumberings.put(Stage.DISCOVERY, 1);
        expectedNumberings.put(Stage.SYNCHRONIZE, 2);
        expectedNumberings.put(Stage.WRITE, 3);

        for (final Stage stage : expectedNumberings.keySet()) {
            Assert.assertEquals(expectedNumberings.get(stage).intValue(), stage.toStageNumber());
        }
    }
}
