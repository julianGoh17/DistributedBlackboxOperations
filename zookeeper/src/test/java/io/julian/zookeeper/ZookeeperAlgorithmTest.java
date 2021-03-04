package io.julian.zookeeper;

import io.julian.ZookeeperAlgorithm;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.ShortenedExchange;
import io.vertx.core.Vertx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

public class ZookeeperAlgorithmTest {
    HashMap<String, Class> map = new HashMap<>();

    @Before
    public void before() {
        map.put("test", Object.class);
        map.put("candidate_information", CandidateInformation.class);
        map.put("state_update", ShortenedExchange.class);
    }

    @Test
    public void TestGetMessageClass() {
        ZookeeperAlgorithm algorithm = new ZookeeperAlgorithm(new Controller(new Configuration()), new MessageStore(), Vertx.vertx());
        for (String key : map.keySet()) {
            Assert.assertEquals(map.get(key), algorithm.getMessageClass(new CoordinationMetadata(HTTPRequest.UNKNOWN, key)));
        }
    }
}
