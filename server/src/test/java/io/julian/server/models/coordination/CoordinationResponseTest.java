package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Test;

public class CoordinationResponseTest {
    @Getter
    @Setter
    public static class TestUserDefinition extends AbstractCoordinationUserDefinition {
        private String user;
        public TestUserDefinition(@JsonProperty("user") final String user) {
            this.user = user;
        }

        @Override
        public JsonObject toJson() {
            return new JsonObject().put("user", user);
        }
    }

    private static final JsonObject MESSAGE = new JsonObject().put("test", "message");
    private static final String USER = "test-user";
    public static final JsonObject JSON = new JsonObject()
        .put("metadata", CoordinationMetadataTest.JSON)
        .put("message", MESSAGE)
        .put("definition", new JsonObject().put("user", USER));

    @Test
    public void TestCoordinationResponseCanMapFromJson() {
        CoordinationResponse<TestUserDefinition> response = CoordinationResponse.fromJson(JSON, TestUserDefinition.class);

        Assert.assertNotNull(response.getMetadata());
        Assert.assertNotNull(response.getDefinition());
        Assert.assertNotNull(response.getMessage());

        Assert.assertEquals(CoordinationMetadataTest.SERVER_ID, response.getMetadata().getFromServerId());
        Assert.assertEquals(CoordinationMetadataTest.TIME, response.getMetadata().getTimestamp().toLocalDateTime());

        Assert.assertEquals(MESSAGE, response.getMessage());
        Assert.assertEquals(USER, response.getDefinition().getUser());
    }

    @Test
    public void TestCoordinationResponseCanMapToJson() {
        CoordinationResponse<TestUserDefinition> response = new CoordinationResponse<>(
            new CoordinationMetadata(CoordinationMetadataTest.SERVER_ID, new CoordinationTimestamp(CoordinationMetadataTest.TIME)),
            MESSAGE,
            new TestUserDefinition(USER));

        Assert.assertEquals(JSON.encodePrettily(), response.toJson().encodePrettily());
    }
}
