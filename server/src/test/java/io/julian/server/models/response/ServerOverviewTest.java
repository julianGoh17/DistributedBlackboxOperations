package io.julian.server.models.response;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ServerOverviewTest {
    private final static String HOST = "random-host";
    private final static int PORT = 1234;
    private final static int NUM_MESSAGES = -34;

    @Test
    public void TestInit() {
        ServerOverview status = new ServerOverview(HOST, PORT, NUM_MESSAGES);
        Assert.assertEquals(HOST, status.getHost());
        Assert.assertEquals(PORT, status.getPort());
        Assert.assertEquals(NUM_MESSAGES, status.getNumMessages());
    }

    @Test
    public void TestToJson() {
        JsonObject json = new JsonObject()
            .put("host", HOST)
            .put("port", PORT)
            .put("numMessages", NUM_MESSAGES);

        Assert.assertEquals(json.encodePrettily(), new ServerOverview(HOST, PORT, NUM_MESSAGES).toJson().encodePrettily());

        ServerOverview status = json.mapTo(ServerOverview.class);
        Assert.assertEquals(HOST, status.getHost());
        Assert.assertEquals(PORT, status.getPort());
        Assert.assertEquals(NUM_MESSAGES, status.getNumMessages());
    }
}
