package io.julian.client.operations;

import io.julian.client.model.MessageIdResponse;
import io.julian.client.model.MessageWrapper;
import io.julian.client.model.GetMessageResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseClient {
    private static final Logger log = LogManager.getLogger(BaseClient.class.getName());
    private static final String CLIENT_URI = "/client";

    private final WebClient client;

    public BaseClient(final Vertx vertx) {
        client = WebClient.create(vertx);
    }

    public Future<String> POSTMessage(final JsonObject message) {
        log.traceEntry(() -> message);
        Promise<String> messageID = Promise.promise();
        client.post(Configuration.getServerPort(), Configuration.getServerHost(), CLIENT_URI)
            .sendJson(new MessageWrapper(message).toJson(), res -> {
                MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                if (response.isError()) {
                    log.error(response.getError());
                    messageID.fail(response.getError());
                } else {
                    log.info(String.format("Successful POST and returned id '%s'", response.getMessageId()));
                    messageID.complete(response.getMessageId());
                }
            });

        return log.traceExit(messageID.future());
    }

    public Future<JsonObject> GETMessage(final String messageId) {
        log.traceEntry(() -> messageId);
        Promise<JsonObject> getResponse = Promise.promise();
        client.get(Configuration.getServerPort(), Configuration.getServerHost(), String.format("%s/%s", CLIENT_URI, messageId))
            .send(res -> {
                GetMessageResponse get = res.result().bodyAsJsonObject().mapTo(GetMessageResponse.class);
                if (get.isError()) {
                    log.error(get.getError());
                    getResponse.fail(get.getError());
                } else {
                    log.info(String.format("Successful GET for message id '%s'", messageId));
                    getResponse.complete(get.getMessage());
                }
            });

        return log.traceExit(getResponse.future());
    }

    public Future<String> PUTMessage(final String messageId, final JsonObject message) {
        log.traceEntry(() -> messageId, () -> message);
        Promise<String> putResponse = Promise.promise();
        client.put(Configuration.getServerPort(), Configuration.getServerHost(), String.format("%s/%s", CLIENT_URI, messageId))
            .sendJsonObject(new MessageWrapper(message).toJson(), res -> {
                MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                if (response.isError()) {
                    log.error(response.getError());
                    putResponse.fail(response.getError());
                } else {
                    log.info(String.format("Successful POST and returned id '%s'", response.getMessageId()));
                    putResponse.complete(response.getMessageId());
                }
            });
        return log.traceExit(putResponse.future());
    }

    public void closeClient() {
        log.traceEntry();
        client.close();
        log.traceExit();
    }
}
