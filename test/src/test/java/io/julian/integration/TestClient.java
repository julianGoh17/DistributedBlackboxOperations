package io.julian.integration;

import io.julian.server.components.Configuration;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class TestClient {
    private final WebClient client;

    public static final CoordinationMessage MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("random-id", HTTPRequest.GET),
        new JsonObject(),
        new JsonObject()
    );

    public TestClient(final Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public Future<HttpResponse<Buffer>> POST_COORDINATE_MESSAGE(final CoordinationMessage message) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, "/coordinate/message")
            .sendJsonObject(message.toJson(), res -> {
                if (res.succeeded()) {
                    response.complete(res.result());
                } else {
                    response.fail(res.cause());
                }
            });
        return response.future();
    }

    public Future<HttpResponse<Buffer>> POST_MESSAGE(final JsonObject message) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, "/client")
            .sendJsonObject(new JsonObject().put("message", message), res -> {
                if (res.succeeded()) {
                    response.complete(res.result());
                } else {
                    response.fail(res.cause());
                }
            });
        return response.future();
    }

    public Future<HttpResponse<Buffer>> DELETE_MESSAGE(final String uuid) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .delete(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, String.format("/client/?messageId=%s", uuid))
            .send(res -> {
                if (res.succeeded()) {
                    response.complete(res.result());
                } else {
                    response.fail(res.cause());
                }
            });
        return response.future();
    }
}
