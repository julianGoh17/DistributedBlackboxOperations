package io.julian.client.operations;

import io.julian.client.exception.ClientException;
import io.julian.client.model.MessageWrapper;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.response.GetMessageResponse;
import io.julian.client.model.response.MessageIdResponse;
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
                if (res.succeeded()) {
                    MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                    if (response.isError()) {
                        ClientException exception = new ClientException(response.getError(), response.getStatusCode());
                        log.error(exception);
                        messageID.fail(exception);
                    } else {
                        log.info(String.format("Successful POST and returned id '%s'", response.getMessageId()));
                        messageID.complete(response.getMessageId());
                    }
                } else {
                    ClientException exception = new ClientException(res.cause().getMessage(), 400);
                    log.error(exception);
                    messageID.fail(exception);
                }
            });

        return log.traceExit(messageID.future());
    }

    public Future<JsonObject> GETMessage(final String messageId, final Expected expected) {
        log.traceEntry(() -> messageId);
        Promise<JsonObject> getResponse = Promise.promise();
        client.get(Configuration.getServerPort(), Configuration.getServerHost(), String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(res -> {
                if (res.succeeded()) {
                    GetMessageResponse get = res.result().bodyAsJsonObject().mapTo(GetMessageResponse.class);
                    if (expected.doesNotMatchExpectedStatusCode(res.result().statusCode())) {
                        ClientException exception = expected.generateMismatchedException(res.result().statusCode(),
                            get.getError());
                        log.error(exception);
                        getResponse.fail(exception);
                    } else {
                        if (get.isError()) {
                            log.info(String.format("Correctly could not GET message id '%s'", messageId));
                            getResponse.complete();
                        } else {
                            log.info(String.format("Successful GET for message id '%s'", messageId));
                            getResponse.complete(get.getMessage());
                        }
                    }
                } else {
                    ClientException exception = new ClientException(res.cause().getMessage(), 400);
                    log.error(exception);
                    getResponse.fail(exception);
                }
            });

        return log.traceExit(getResponse.future());
    }

    public Future<String> DELETEMessage(final String messageId, final Expected expected) {
        log.traceEntry(() -> messageId);
        Promise<String> delete = Promise.promise();
        client.delete(Configuration.getServerPort(), Configuration.getServerHost(), String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(res -> {
                if (res.succeeded()) {
                    MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                    if (expected.doesNotMatchExpectedStatusCode(res.result().statusCode())) {
                        ClientException exception = expected.generateMismatchedException(res.result().statusCode(),
                            response.getError());
                        log.error(exception);
                        delete.fail(exception);
                    } else {
                        if (response.isError()) {
                            log.info(String.format("Correctly could not DELETE message id '%s'", messageId));
                            delete.complete();
                        } else {
                            log.info(String.format("Successful DELETE for message id '%s'", messageId));
                            delete.complete(response.getMessageId());
                        }
                    }
                } else {
                    ClientException exception = new ClientException(res.cause().getMessage(), 400);
                    log.error(exception);
                    delete.fail(exception);
                }
            });

        return log.traceExit(delete.future());
    }

    public void closeClient() {
        log.traceEntry();
        client.close();
        log.traceExit();
    }
}
