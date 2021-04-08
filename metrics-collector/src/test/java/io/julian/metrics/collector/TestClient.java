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
    private final static String REPORT_URI = "/report";

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

    public void unsuccessfulTrackMessage(final TestContext context, final JsonObject json, final String error, final int statusCode) {
        Async async = context.async();
        trackMessage(json)
            .onComplete(context.asyncAssertSuccess(res -> {
                ErrorResponse response = res.mapTo(ErrorResponse.class);
                Assert.assertEquals(error, response.getError().getMessage());
                Assert.assertEquals(statusCode, response.getStatusCode());
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
                    json.complete(res.result().bodyAsJsonObject());
                } else {
                    json.complete(new ErrorResponse(500, res.cause()).toJson());
                }
            });
        return json.future();
    }

    public void successfulCreateReport(final TestContext context, final String filter) {
        Async async = context.async();
        createReport(filter)
            .onComplete(context.asyncAssertSuccess(res -> {
                SuccessResponse response = res.mapTo(SuccessResponse.class);
                Assert.assertEquals(200, response.getStatusCode());
                async.complete();
            }));
        async.awaitSuccess();
    }

    public void unsuccessfulCreateReport(final TestContext context, final String filter, final String error, final int statusCode) {
        Async async = context.async();
        createReport(filter)
            .onComplete(context.asyncAssertSuccess(res -> {
                ErrorResponse response = res.mapTo(ErrorResponse.class);
                Assert.assertEquals(error, response.getError().getMessage());
                Assert.assertEquals(statusCode, response.getStatusCode());
                async.complete();
            }));
        async.awaitSuccess();
    }

    public Future<JsonObject> createReport(final String filter) {
        Promise<JsonObject> json = Promise.promise();
        this.client
            .post(PORT, HOST, String.format("%s?filterName=%s", REPORT_URI, filter))
            .send(res -> {
                if (res.succeeded()) {
                    json.complete(res.result().bodyAsJsonObject());
                } else {
                    json.complete(new ErrorResponse(500, res.cause()).toJson());
                }
            });
        return json.future();
    }
}
