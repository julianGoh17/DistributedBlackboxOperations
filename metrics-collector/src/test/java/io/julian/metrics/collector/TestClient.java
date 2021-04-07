package io.julian.metrics.collector;

import io.julian.metrics.collector.models.ErrorResponse;
import io.julian.metrics.collector.models.SuccessResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import org.junit.Assert;

public class TestClient {
    private final static String HOST = TestServerComponents.HOST;
    private final static int PORT = TestServerComponents.PORT;
    private final WebClient client;

    private final static String TRACK_URI = "/track";

    public TestClient(final Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public void successfulTrackMessage(final TestContext context, final JsonObject json) {
        Async async = context.async();
        trackMessage(json)
            .onComplete(context.asyncAssertSuccess(res -> {
                SuccessResponse response = res.mapTo(SuccessResponse.class);
                Assert.assertEquals(200, response.getStatusCode());
                async.complete();
            }));
        async.awaitSuccess();
    }

    public void unsuccessfulTrackMessage(final TestContext context, final JsonObject json, final String error) {
        Async async = context.async();
        trackMessage(json)
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(error, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }
    public Future<JsonObject> trackMessage(final JsonObject object) {
        Promise<JsonObject> json = Promise.promise();
        this.client
            .post(PORT, HOST, TRACK_URI)
            .sendJsonObject(object, res -> {
                if (res.succeeded()) {
                    if (res.result().statusCode() == 200) {
                        json.complete(res.result().bodyAsJsonObject());
                    } else {
                        ErrorResponse response = res.result().bodyAsJsonObject().mapTo(ErrorResponse.class);
                        json.fail(response.getError());
                    }
                } else {
                    json.fail(res.cause());
                }
            });
        return json.future();
    }
}
