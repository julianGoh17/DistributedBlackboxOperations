package io.julian.server.models.coordination;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class CoordinationMetadataTest {
    public final static String SERVER_ID = "test-id";
    public final static LocalDateTime TIME = LocalDateTime
        .now();

    public final static JsonObject JSON = new JsonObject()
        .put("fromServerId", SERVER_ID)
        .put("timestamp", TIME.toString());

    @Test
    public void TestCoordinationMetadataCanMapFromJson() {
        CoordinationMetadata metadata = JSON.mapTo(CoordinationMetadata.class);
        Assert.assertEquals(SERVER_ID, metadata.getFromServerId());
        Assert.assertEquals(TIME, metadata.getTimestamp().toLocalDateTime());
    }

    @Test
    public void TestCoordinationMetadataCanMapToJson() {
        CoordinationMetadata metadata = new CoordinationMetadata(SERVER_ID, new CoordinationTimestamp(TIME));
        JsonObject json = metadata.toJson();
        Assert.assertEquals(SERVER_ID, json.getString(CoordinationMetadata.FROM_SERVER_ID_KEY));
        Assert.assertEquals(TIME.toString(), json.getString(CoordinationMetadata.TIMESTAMP_KEY));
    }
}
