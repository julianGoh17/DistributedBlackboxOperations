package io.julian.zookeeper;

import io.julian.MessageHandler;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandlerTest extends AbstractServerBase {
    HashMap<String, Class> map = new HashMap<>();

    @Before
    public void before() {
        super.before();
        map.put("test", Object.class);
        map.put("candidate_information", CandidateInformation.class);
        map.put("state_update", ShortenedExchange.class);
        map.put("discovery", Zxid.class);
        map.put("leader_state_update", Zxid.class);
        map.put("forward", ClientMessage.class);
        map.put("synchronize", State.class);
    }

    @Test
    public void TestGetMessageClass() {
        MessageHandler handler = createTestHandler();
        for (String key : map.keySet()) {
            Assert.assertEquals(map.get(key), handler.getMessageClass(new CoordinationMetadata(HTTPRequest.UNKNOWN, null, key)));
        }
    }

    @Test
    public void TestGenerateRandomNumberWithManyDigits() {
        MessageHandler handler = createTestHandler();
        long manyDigitNumber = handler.generateCandidateNumber(0);

        Assert.assertNotEquals(manyDigitNumber, handler.generateCandidateNumber(0));
        Assert.assertTrue(manyDigitNumber > 0);
    }

    @Test
    public void TestInitializeCandidateRegistry() {
        MessageHandler handler = createTestHandler();
        CandidateInformationRegistry registry = handler.initializeCandidateInformationRegistry(new Configuration(), 1);
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, registry.getCandidateNumberAndInformationMap().get(1L).getHost());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, registry.getCandidateNumberAndInformationMap().get(1L).getPort());
    }

    private MessageHandler createTestHandler() {
        return new MessageHandler(new Controller(new Configuration()), new MessageStore(), this.vertx, createTestRegistryManager(), createServerClient(), new ConcurrentLinkedQueue<>());
    }
}
