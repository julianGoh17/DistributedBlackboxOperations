package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonProperty;
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

        Assert.assertEquals(CoordinationMetadataTest.SERVER_ID, response.getMetadata().getFromServerId());
        Assert.assertEquals(CoordinationMetadataTest.TIME, response.getMetadata().getTimestamp().toLocalDateTime());

        Assert.assertEquals(MESSAGE, response.getMessage());
        TestUserDefinition definition = response.getDefinition().mapTo(TestUserDefinition.class);
        Assert.assertEquals(USER, definition.getUser());
    }

    @Test
    public void TestCoordinationResponseCanMapToJson() {
        CoordinationMessage response = new CoordinationMessage(
            new CoordinationMetadata(CoordinationMetadataTest.SERVER_ID, new CoordinationTimestamp(CoordinationMetadataTest.TIME)),
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
    public void TestCoordinationMessageThrowsDecodeExceptionWhenMissingMessageField() {
        JsonObject missingMessageField = JSON.copy();
        missingMessageField.remove(CoordinationMessage.MESSAGE_KEY);

        try {
            CoordinationMessage.fromJson(missingMessageField);
            Assert.fail();
        } catch (DecodeException e) {
            Assert.assertEquals(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.MESSAGE_KEY), e.getMessage());
        }
    }

    @Test
    public void TestCoordinationMessageThrowsDecodeExceptionWhenMissingDefinitionField() {
        JsonObject missingMessageField = JSON.copy();
        missingMessageField.remove(CoordinationMessage.DEFINITION_KEY);

        try {
            CoordinationMessage.fromJson(missingMessageField);
            Assert.fail();
        } catch (DecodeException e) {
            Assert.assertEquals(String.format(CoordinationMessage.DECODE_EXCEPTION_FORMAT_STRING, CoordinationMessage.DEFINITION_KEY), e.getMessage());
        }
    }
}
