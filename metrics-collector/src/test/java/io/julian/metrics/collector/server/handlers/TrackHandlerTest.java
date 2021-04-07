package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.TestClient;
import io.julian.metrics.collector.TestServerComponents;
import io.julian.metrics.collector.models.TrackedMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TrackHandlerTest extends AbstractHandlerTest {
    private final static TrackedMessage MESSAGE = new TrackedMessage(200, "random-id", 10.5f);

    @Test
    public void TestCanSuccessfullyTrackMessage(final TestContext context) {
        TestServerComponents server = startServer(context);
        TestClient client = createTestClient();

        server.testHasExpectedStatusSize(0);
        client.successfulTrackMessage(context, MESSAGE.toJson());
        server.testHasExpectedStatusSize(1);

        server.tearDownServer(context);
    }

    @Test
    public void TestCanDoesNotCreateNewKeyForTrackedMessage(final TestContext context) {
        TestServerComponents server = startServer(context);
        TestClient client = createTestClient();

        server.testHasExpectedStatusSize(0);
        client.successfulTrackMessage(context, MESSAGE.toJson());
        server.testHasExpectedStatusSize(1);
        client.successfulTrackMessage(context, MESSAGE.toJson());
        server.testHasExpectedStatusSize(1);

        server.tearDownServer(context);
    }

    @Test
    public void TestErrorsWhenMissingFields(final TestContext context) {
        TestServerComponents server = startServer(context);
        TestClient client = createTestClient();

        List<String> keys = Arrays.asList(TrackedMessage.MESSAGE_ID_KEY, TrackedMessage.MESSAGE_SIZE_KEY, TrackedMessage.STATUS_CODE_KEY);

        for (String key : keys) {
            JsonObject json = MESSAGE.toJson().copy();
            json.remove(key);
            client.unsuccessfulTrackMessage(context, json, String.format("$.%s: is missing but it is required", key), 400);
            server.testHasExpectedStatusSize(0);
        }

        server.tearDownServer(context);
    }
}
