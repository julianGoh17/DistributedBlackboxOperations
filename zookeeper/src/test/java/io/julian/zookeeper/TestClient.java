package io.julian.zookeeper;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

public class TestClient {
    public final static String CLIENT_URI = "/client";

    private final WebClient client;

    public TestClient(final Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public Future<HttpResponse<Buffer>> POST_MESSAGE(final String host, final int port, final JsonObject message) {
        Promise<HttpResponse<Buffer>> response = Promise.promise();
        client
            .post(port, host, CLIENT_URI)
            .sendJsonObject(new JsonObject().put("message", message), res -> {
                if (res.succeeded()) {
                    response.complete(res.result());
                } else {
                    response.fail(res.cause());
                }
            });
        return response.future();
    }
}
