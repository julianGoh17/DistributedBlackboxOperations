package io.julian.server.models.response;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class ServerOverviewTest {
    private final static String HOST = "random-host";
    private final static int PORT = 1234;
    private final static int NUM_MESSAGES = -34;
    private final static List<String> MESSAGE_IDS = Collections.singletonList("babe");

    @Test
    public void TestInit() {
        ServerOverview status = new ServerOverview(HOST, PORT, NUM_MESSAGES, MESSAGE_IDS);
        Assert.assertEquals(HOST, status.getHost());
        Assert.assertEquals(PORT, status.getPort());
        Assert.assertEquals(NUM_MESSAGES, status.getNumMessages());
        for (int i = 0; i < status.getNumMessages(); i++) {
            Assert.assertEquals(MESSAGE_IDS.get(i), status.getMessageIds().get(0));
        }
    }

    @Test
    public void TestToJson() {
        JsonObject json = new JsonObject()
            .put("host", HOST)
            .put("port", PORT)
            .put("numMessages", NUM_MESSAGES)
            .put("messageIds", MESSAGE_IDS);

        Assert.assertEquals(json.encodePrettily(), new ServerOverview(HOST, PORT, NUM_MESSAGES, MESSAGE_IDS).toJson().encodePrettily());

        ServerOverview status = json.mapTo(ServerOverview.class);
        Assert.assertEquals(HOST, status.getHost());
        Assert.assertEquals(PORT, status.getPort());
        Assert.assertEquals(NUM_MESSAGES, status.getNumMessages());
        for (int i = 0; i < status.getNumMessages(); i++) {
            Assert.assertEquals(MESSAGE_IDS.get(i), status.getMessageIds().get(0));
        }
    }
}
