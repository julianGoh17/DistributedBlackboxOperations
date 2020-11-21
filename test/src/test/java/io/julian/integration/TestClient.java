package io.julian.integration;

import io.julian.server.components.Configuration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.server.models.coordination.CoordinationTimestamp;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.time.LocalDateTime;

public class TestClient {
    private final WebClient client;

    public static final CoordinationMessage MESSAGE = new CoordinationMessage(
        new CoordinationMetadata("random-id", new CoordinationTimestamp(LocalDateTime.now())),
        new JsonObject(),
        new JsonObject()
    );

    public TestClient(final Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public Future<HttpResponse<Buffer>> POST(final CoordinationMessage message) {
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
}
