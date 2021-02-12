package io.julian.server.client;

import io.julian.server.models.control.OtherServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.response.ErrorResponse;
import io.julian.server.models.response.LabelResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerClient {
    public static final String COORDINATOR_URI = "/coordinate";
    public static final String COORDINATION_MESSAGE_ENDPOINT = "message";
    public static final String LABEL_SERVER_ENDPOINT = "label";

    private final Logger log = LogManager.getLogger(ServerClient.class.getName());
    private final WebClient client;

    public ServerClient(final Vertx vertx) {
        this.client = WebClient.create(vertx);
    }

    public Future<Void> sendCoordinateMessageToServer(final OtherServerConfiguration configuration, final CoordinationMessage message) {
        log.traceEntry(() -> configuration, () -> message);
        log.info(String.format("Sending POST Coordinate Message to %s", configuration.toString()));

        Promise<Void> result = Promise.promise();
        client.post(configuration.getPort(), configuration.getHost(), String.format("%s/%s", COORDINATOR_URI, COORDINATION_MESSAGE_ENDPOINT))
            .sendJsonObject(message.toJson(), res -> {
                if (res.succeeded()) {
                    if (res.result().statusCode() == 200) {
                        log.info(String.format("Successful POST coordinate message to server '%s/%d'",
                            configuration.getHost(), configuration.getPort()));
                        result.complete();
                    } else {
                        ErrorResponse response = ErrorResponse.fromJson(res.result().bodyAsJsonObject());
                        log.error(response.getException());
                        result.fail(response.getException());
                    }
                } else {
                    ErrorResponse response = new ErrorResponse(500, res.cause());
                    log.error(response.getException());
                    result.fail(response.getException());
                }
            });

        return log.traceExit(result.future());
    }

    public Future<Void> sendLabelToServer(final OtherServerConfiguration configuration, final String label) {
        log.traceEntry(() -> configuration, () -> label);
        Promise<Void> result = Promise.promise();
        log.info(String.format("Sending POST to %s to update with label '%s'", configuration.toString(), label));
        client.post(configuration.getPort(), configuration.getHost(), String.format("%s/%s?label=%s", COORDINATOR_URI, LABEL_SERVER_ENDPOINT, label))
            .send(res -> {
                if (res.succeeded()) {
                    if (res.result().statusCode() == 202) {
                        log.info(String.format("Successful POST label to %s", configuration.toString()));
                        configuration.setLabel(label);
                        result.complete();
                    } else {
                        ErrorResponse response = ErrorResponse.fromJson(res.result().bodyAsJsonObject());
                        log.error(response.getException());
                        result.fail(response.getException());
                    }
                } else {
                    ErrorResponse response = new ErrorResponse(500, res.cause());
                    log.error(response.getException());
                    result.fail(response.getException());
                }
            });
        return log.traceExit(result.future());
    }

    public Future<LabelResponse> getServerLabel(final OtherServerConfiguration configuration) {
        log.traceEntry(() -> configuration);
        Promise<LabelResponse> label = Promise.promise();
        log.info(String.format("Sending GET Label request to %s", configuration.toString()));
        client
            .get(configuration.getPort(), configuration.getHost(), String.format("%s/%s", COORDINATOR_URI, LABEL_SERVER_ENDPOINT))
            .send(ar -> {
                if (ar.succeeded()) {
                    if (ar.result().statusCode() == 200) {
                        log.info(String.format("Successful GET label from %s and updated configuration",
                            configuration.toString()));
                        LabelResponse retrievedLabel = ar.result().bodyAsJsonObject().mapTo(LabelResponse.class);
                        configuration.setLabel(retrievedLabel.getLabel());
                        label.complete(retrievedLabel);
                    } else {
                        ErrorResponse response = ErrorResponse.fromJson(ar.result().bodyAsJsonObject());
                        log.error(response.getException());
                        label.fail(response.getException());
                    }
                } else {
                    ErrorResponse response = new ErrorResponse(500, ar.cause());
                    log.error(response.getException());
                    label.fail(response.getException());
                }
            });

        return log.traceExit(label.future());
    }

    public WebClient getWebClient() {
        log.traceEntry();
        return log.traceExit(client);
    }
}
