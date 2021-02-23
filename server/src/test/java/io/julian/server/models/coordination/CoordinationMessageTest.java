package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.server.models.HTTPRequest;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

public class CoordinationMessageTest {
    @Getter
    @Setter
    public static class TestUserDefinition {
        private String user;
        public TestUserDefinition(@JsonProperty("user") final String user) {
            this.user = user;
        }
    }

    private static final JsonObject MESSAGE = new JsonObject().put("test", "message");
    private static final String USER = "test-user";
    public static final JsonObject JSON = new JsonObject()
        .put(CoordinationMessage.METADATA_KEY, CoordinationMetadataTest.JSON)
        .put(CoordinationMessage.MESSAGE_KEY, MESSAGE)
        .put(CoordinationMessage.DEFINITION_KEY, new JsonObject().put("user", USER));

    @Test
    public void TestCoordinationResponseCanMapFromJson() {
        CoordinationMessage response = CoordinationMessage.fromJson(JSON);

        Assert.assertNotNull(response.getMetadata());
        Assert.assertNotNull(response.getDefinition());
        Assert.assertNotNull(response.getMessage());

        Assert.assertEquals(CoordinationMetadataTest.TIME, response.getMetadata().getTimestamp().toLocalDateTime());

        Assert.assertEquals(MESSAGE, response.getMessage());
        TestUserDefinition definition = response.getDefinition().mapTo(TestUserDefinition.class);
        Assert.assertEquals(USER, definition.getUser());
    }

    @Test
    public void TestCoordinationResponseCanMapToJson() {
        CoordinationMessage response = new CoordinationMessage(
            new CoordinationMetadata(new CoordinationTimestamp(CoordinationMetadataTest.TIME),
                CoordinationMetadataTest.REQUEST, CoordinationMetadataTest.MESSAGE_ID),
            MESSAGE,
            new JsonObject().put("user", USER));

        Assert.assertEquals(JSON.encodePrettily(), response.toJson().encodePrettily());
    }

    @Test
    public void TestCoordinationMessageThrowsDecodeExceptionWhenMissingMetadataField() {
        JsonObject missingMetadataField = JSON.copy();
        missingMetadataField.remove(CoordinationMessage.METADATA_KEY);

        try {
            CoordinationMessage.fromJson(missingMetadataField);
            Assert.fail();
        } catch (DecodeException e) {
            Assert.assertEquals(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.METADATA_KEY), e.getMessage());
        }
    }

    @Test
    public void TestCoordinateMessageCanMapToJsonWithoutClientMessage() {
        JsonObject userObject = new JsonObject().put("test", 1234);
        CoordinationMessage message = new CoordinationMessage(HTTPRequest.DELETE, userObject);

        JsonObject json = message.toJson();
        Assert.assertNotNull(json.getJsonObject(CoordinationMessage.METADATA_KEY));
        Assert.assertNull(json.getJsonObject(CoordinationMessage.MESSAGE_KEY));
        Assert.assertEquals(userObject.encodePrettily(), json.getJsonObject(CoordinationMessage.DEFINITION_KEY).encodePrettily());


        CoordinationMessage mapped = CoordinationMessage.fromJson(json);
        Assert.assertNull(mapped.getMessage());
        Assert.assertEquals(message.getDefinition().encodePrettily(), mapped.getDefinition().encodePrettily());
    }
}
