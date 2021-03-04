package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.models.CandidateInformationTest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class BroadcastCandidateInformationHandlerTest extends AbstractServerBase {
    @Test
    public void TestBroadCastHandlerCanSendMessage(final TestContext context) {
        TestServerComponents server = setUpApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        BroadcastCandidateInformationHandler handler = new BroadcastCandidateInformationHandler();
        RegistryManager manager = createTestRegistryManager();
        ServerClient client = createServerClient();

        handler.broadcast(manager, client, CandidateInformationTest.CANDIDATE_NUMBER,
            new ServerConfiguration(CandidateInformationTest.HOST, CandidateInformationTest.PORT))
            .onComplete(context.asyncAssertSuccess(future -> {
                context.assertEquals(1, future.size());
                context.assertTrue(future.succeeded(0));
            }));
        tearDownServer(context, server);
    }

    @Test
    public void TestBroadCastHandlerFailsToSend(final TestContext context) {
        BroadcastCandidateInformationHandler handler = new BroadcastCandidateInformationHandler();
        RegistryManager manager = createTestRegistryManager();
        ServerClient client = createServerClient();

        handler.broadcast(manager, client, CandidateInformationTest.CANDIDATE_NUMBER,
            new ServerConfiguration(CandidateInformationTest.HOST, CandidateInformationTest.PORT))
            .onComplete(context.asyncAssertFailure(future ->
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, future.getMessage())));
    }

    @Test
    public void TestCreateCoordinateMessage() {
        ServerConfiguration configuration = new ServerConfiguration(CandidateInformationTest.HOST, CandidateInformationTest.PORT);
        BroadcastCandidateInformationHandler handler = new BroadcastCandidateInformationHandler();

        CoordinationMessage message = handler.createCandidateInformationMessage(CandidateInformationTest.CANDIDATE_NUMBER,
            configuration);

        Assert.assertNull(message.getMessage());
        Assert.assertEquals(CandidateInformationTest.JSON.encodePrettily(), message.getDefinition().encodePrettily());
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());

        JsonObject object = message.toJson();

        Assert.assertNull(object.getJsonObject(CoordinationMessage.MESSAGE_KEY));
        Assert.assertNotNull(object.getJsonObject(CoordinationMessage.METADATA_KEY));
        Assert.assertEquals(BroadcastCandidateInformationHandler.TYPE, object.getJsonObject(CoordinationMessage.METADATA_KEY).getString(CoordinationMetadata.TYPE_KEY));
        Assert.assertEquals(CandidateInformationTest.JSON.encodePrettily(),
            object.getJsonObject(CoordinationMessage.DEFINITION_KEY).encodePrettily());
    }
}
