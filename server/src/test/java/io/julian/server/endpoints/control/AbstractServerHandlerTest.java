package io.julian.server.endpoints.control;

import io.julian.server.components.Configuration;
import io.julian.server.endpoints.AbstractHandlerTest;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ServerSettings;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.junit.Assert;

public abstract class AbstractServerHandlerTest extends AbstractHandlerTest {
    protected final static String SET_SETTINGS_ENDPOINT = "/server";

    protected Future<Object> POSTSuccessfulServerSettings(final TestContext context, final WebClient client, final ServerSettings settings,
                                       final ServerStatus expectedStatus, final float expectedFailureChance) {
        return POSTServerSettings(context, client, settings.toJson())
            .compose(res -> {
                context.assertEquals(res.statusCode(), 202);
                context.assertEquals(settings.toJson().encodePrettily(), res.bodyAsJsonObject().encodePrettily());
                context.assertEquals(expectedStatus, server.getController().getStatus());
                context.assertEquals(expectedFailureChance, server.getController().getFailureChance());
                return Future.succeededFuture();
            });
    }

    protected Future<HttpResponse<Buffer>> POSTServerSettings(final TestContext context, final WebClient client, final JsonObject message) {
        Promise<HttpResponse<Buffer>> postRes = Promise.promise();

        if (message != null) {
            client
                .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, SET_SETTINGS_ENDPOINT)
                .sendJsonObject(message, context.asyncAssertSuccess(postRes::complete));
        } else {
            client
                .post(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, SET_SETTINGS_ENDPOINT)
                .send(context.asyncAssertSuccess(postRes::complete));
        }

        return postRes.future();
    }

    protected Future<Void> GETAndAssertServerSettings(final TestContext context, final WebClient client,
                                            final ServerStatus expectedStatus, final float expectedFailureChance) {
        Promise<Void> get = Promise.promise();
        GETServerSettings(context, client)
            .onComplete(context.asyncAssertSuccess(res -> {
                ServerSettings receivedSettings = res.bodyAsJsonObject().mapTo(ServerSettings.class);
                Assert.assertEquals(expectedFailureChance, receivedSettings.getFailureChance(), 0);
                Assert.assertEquals(expectedStatus, receivedSettings.getStatus());
                Assert.assertEquals(expectedFailureChance, server.getController().getFailureChance(), 0);
                Assert.assertEquals(expectedStatus, server.getController().getStatus());
                get.complete();
            }));
        return get.future();
    }

    private Future<HttpResponse<Buffer>> GETServerSettings(final TestContext context, final WebClient client) {
        Promise<HttpResponse<Buffer>> get = Promise.promise();

        client
            .get(Configuration.DEFAULT_SERVER_PORT, Configuration.DEFAULT_SERVER_HOST, SET_SETTINGS_ENDPOINT)
            .send(context.asyncAssertSuccess(get::complete));

        return get.future();
    }
}
