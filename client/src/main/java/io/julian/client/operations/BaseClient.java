package io.julian.client.operations;

import io.julian.client.exception.ClientException;
import io.julian.client.model.MessageWrapper;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.response.GetMessageResponse;
import io.julian.client.model.response.MessageIdResponse;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.response.ServerOverview;
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
    private static final String OVERVIEW_URI = String.format("%s/overview", CLIENT_URI);

    private final WebClient client;
    private final ClientConfiguration clientConfiguration;

    public BaseClient(final Vertx vertx, final ClientConfiguration clientConfiguration) {
        client = WebClient.create(vertx);
        this.clientConfiguration = clientConfiguration;
    }

    public Future<String> POSTMessage(final JsonObject message, final Expected expected) {
        log.traceEntry(() -> message);
        Promise<String> messageID = Promise.promise();
        log.info(String.format("%s sending POST request '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
        client.post(clientConfiguration.getServerPort(), clientConfiguration.getServerHost(), CLIENT_URI)
            .sendJson(new MessageWrapper(message).toJson(), res -> {
                int statusCode = res.result() != null ? res.result().statusCode() : 500;
                if (expected.doesNotMatchExpectedStatusCode(statusCode)) {
                    log.info(String.format("%s POST request received an unexpected status code (%d) than expected (%d)", BaseClient.class.getSimpleName(), expected.getStatusCode(), statusCode));
                    String error = res.cause() != null ? res.cause().getMessage() : res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class).getError();
                    ClientException exception = expected.generateMismatchedException(statusCode,
                        error);
                    log.error(exception);
                    messageID.fail(exception);
                } else if (res.succeeded()) {
                    MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                    if (response.isError()) {
                        log.info(String.format("%s successfully expected failed POST request", BaseClient.class.getSimpleName()));
                        messageID.complete();
                    } else {
                        log.info(String.format("%s successful POST and returned id '%s'", BaseClient.class.getSimpleName(), response.getMessageId()));
                        messageID.complete(response.getMessageId());
                    }
                } else {
                    log.info(String.format("%s successfully expected failed POST request", BaseClient.class.getSimpleName()));
                    messageID.complete();
                }
            });

        return log.traceExit(messageID.future());
    }

    public Future<JsonObject> GETMessage(final String messageId, final Expected expected) {
        log.traceEntry(() -> messageId);
        Promise<JsonObject> getResponse = Promise.promise();
        log.info(String.format("%s sending GET request '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
        client.get(clientConfiguration.getServerPort(), clientConfiguration.getServerHost(), String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(res -> {
                int statusCode = res.result() != null ? res.result().statusCode() : 500;
                if (expected.doesNotMatchExpectedStatusCode(statusCode)) {
                    log.info(String.format("%s GET request received an unexpected status code (%d) than expected (%d)", BaseClient.class.getSimpleName(), expected.getStatusCode(), statusCode));
                    String error = res.cause() != null ? res.cause().getMessage() : res.result().bodyAsJsonObject().mapTo(GetMessageResponse.class).getError();
                    ClientException exception = expected.generateMismatchedException(statusCode,
                        error);
                    log.error(exception);
                    getResponse.fail(exception);
                } else if (res.succeeded()) {
                    GetMessageResponse get = res.result().bodyAsJsonObject().mapTo(GetMessageResponse.class);
                    if (get.isError()) {
                        log.info(String.format("%s successfully expected failed GET request for id '%s'", BaseClient.class.getSimpleName(), messageId));
                        getResponse.complete();
                    } else {
                        log.info(String.format("%s Successful GET request for message id '%s'", BaseClient.class.getSimpleName(), messageId));
                        getResponse.complete(get.getMessage());
                    }
                } else {
                    log.info(String.format("%s successfully expected failed GET request", BaseClient.class.getSimpleName()));
                    getResponse.complete();
                }
            });

        return log.traceExit(getResponse.future());
    }

    public Future<ServerOverview> GetOverview(final ServerConfiguration configuration) {
        log.traceEntry();
        Promise<ServerOverview> getResponse = Promise.promise();
        log.info(String.format("%s attempting to retrieve overview of server '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
        client.get(configuration.getPort(), "localhost", OVERVIEW_URI)
            .send(res -> {
                if (res.succeeded()) {
                    log.info(String.format("%s successfully retrieved overview of server '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
                    getResponse.complete(res.result().bodyAsJsonObject().mapTo(ServerOverview.class));
                } else {
                    log.info(String.format("%s failed to retrieve overview of server '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
                    getResponse.fail(res.cause());
                }
            });

        return log.traceExit(getResponse.future());
    }

    public Future<String> DELETEMessage(final String messageId, final Expected expected) {
        log.traceEntry(() -> messageId);
        Promise<String> delete = Promise.promise();
        log.info(String.format("%s sending DELETE request '%s:%d'", BaseClient.class.getSimpleName(), clientConfiguration.getServerHost(), clientConfiguration.getServerPort()));
        client.delete(clientConfiguration.getServerPort(), clientConfiguration.getServerHost(), String.format("%s/?messageId=%s", CLIENT_URI, messageId))
            .send(res -> {
                int statusCode = res.result() != null ? res.result().statusCode() : 500;
                if (expected.doesNotMatchExpectedStatusCode(statusCode)) {
                    log.info(String.format("%s DELETE request received an unexpected status code (%d) than expected (%d)", BaseClient.class.getSimpleName(), expected.getStatusCode(), statusCode));
                    String error = res.cause() != null ? res.cause().getMessage() : res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class).getError();
                    ClientException exception = expected.generateMismatchedException(statusCode,
                        error);
                    log.error(exception);
                    delete.fail(exception);
                } else if (res.succeeded()) {
                    MessageIdResponse response = res.result().bodyAsJsonObject().mapTo(MessageIdResponse.class);
                    if (response.isError()) {
                        log.info(String.format("%s successfully expected failed DELETE request id '%s'", BaseClient.class.getSimpleName(), messageId));
                        delete.complete();
                    } else {
                        log.info(String.format("%s successful DELETE request for message id '%s'", BaseClient.class.getSimpleName(), messageId));
                        delete.complete(response.getMessageId());
                    }
                } else {
                    log.info(String.format("%s successfully expected failed DELETE request", BaseClient.class.getSimpleName()));
                    delete.complete();
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
