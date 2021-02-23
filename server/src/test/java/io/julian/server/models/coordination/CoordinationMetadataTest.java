package io.julian.server.models.coordination;

import io.julian.server.models.HTTPRequest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class CoordinationMetadataTest {
    public final static LocalDateTime TIME = LocalDateTime
        .now();
    public final static String STRING_REQUEST = "GET";
    public final static HTTPRequest REQUEST = HTTPRequest.forValue(STRING_REQUEST);
    public final static String MESSAGE_ID = "message-id";

    public final static JsonObject JSON = new JsonObject()
        .put("timestamp", TIME.toString())
        .put("request", STRING_REQUEST)
        .put("messageId", MESSAGE_ID);

    @Test
    public void TestCoordinationMetadataCanMapFromJson() {
        CoordinationMetadata metadata = JSON.mapTo(CoordinationMetadata.class);
        Assert.assertEquals(TIME, metadata.getTimestamp().toLocalDateTime());
        Assert.assertEquals(REQUEST, metadata.getRequest());
        Assert.assertEquals(MESSAGE_ID, metadata.getMessageID());
    }

    @Test
    public void TestCoordinationMetadataCanMapToJson() {
        CoordinationMetadata metadata = new CoordinationMetadata(new CoordinationTimestamp(TIME), REQUEST, MESSAGE_ID);
        JsonObject json = metadata.toJson();
        Assert.assertEquals(TIME.toString(), json.getString(CoordinationMetadata.TIMESTAMP_KEY));
        Assert.assertEquals(STRING_REQUEST, json.getString(CoordinationMetadata.REQUEST_KEY));
        Assert.assertEquals(MESSAGE_ID, json.getString(CoordinationMetadata.MESSAGE_ID_KEY));
    }

    @Test
    public void TestCoordinationMetadataWithNullFieldsToJson() {
        CoordinationMetadata metadata = new CoordinationMetadata(REQUEST);
        JsonObject json = metadata.toJson();

        Assert.assertNotNull(json.getString(CoordinationMetadata.TIMESTAMP_KEY));
        Assert.assertEquals(STRING_REQUEST, json.getString(CoordinationMetadata.REQUEST_KEY));
        Assert.assertNull(json.getString(CoordinationMetadata.MESSAGE_ID_KEY));
    }

    @Test
    public void TestCoordinationMetadataCanMapFromNullFields() {
        JsonObject json = JSON.copy();
        json.remove(CoordinationMetadata.MESSAGE_ID_KEY);

        CoordinationMetadata metadata = json.mapTo(CoordinationMetadata.class);
        Assert.assertEquals(TIME.toString(), metadata.getTimestamp().toValue());
        Assert.assertEquals(REQUEST, metadata.getRequest());
        Assert.assertNull(metadata.getMessageID());
    }
}
