package io.julian.server.models.control;

import io.julian.server.models.ServerStatus;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ServerSettingsTest {
    private final static ServerStatus STATUS = ServerStatus.AVAILABLE;
    private final static float FAILURE_CHANCE = 0.5f;

    private final static JsonObject EXPECTED_JSON = new JsonObject()
        .put(ServerSettings.STATUS_KEY, "available")
        .put(ServerSettings.FAILURE_CHANCE_KEY, FAILURE_CHANCE);

    @Test
    public void TestFromValue() {
        ServerSettings settings = EXPECTED_JSON.mapTo(ServerSettings.class);
        Assert.assertEquals(STATUS, settings.getStatus());
        Assert.assertEquals(FAILURE_CHANCE, settings.getFailureChance(), 0);
    }

    @Test
    public void TestToJson() {
        ServerSettings settings = new ServerSettings(STATUS, FAILURE_CHANCE);
        Assert.assertEquals(EXPECTED_JSON.encodePrettily(), settings.toJson().encodePrettily());
    }
}
