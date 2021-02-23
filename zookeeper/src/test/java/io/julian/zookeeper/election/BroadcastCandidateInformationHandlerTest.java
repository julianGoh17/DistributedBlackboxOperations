package io.julian.zookeeper.election;

import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.models.CandidateLeadershipInformationTest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class BroadcastCandidateInformationHandlerTest extends AbstractServerBase {
    @Test
    public void TestCreateCoordinateMessage() {
        ServerConfiguration configuration = new ServerConfiguration(CandidateLeadershipInformationTest.HOST, CandidateLeadershipInformationTest.PORT);
        BroadcastCandidateInformationHandler handler = new BroadcastCandidateInformationHandler();

        CoordinationMessage message = handler.createCandidateInformationMessage(CandidateLeadershipInformationTest.CANDIDATE_NUMBER,
            configuration);

        Assert.assertNull(message.getMessage());
        Assert.assertEquals(CandidateLeadershipInformationTest.JSON.encodePrettily(), message.getDefinition().encodePrettily());
        Assert.assertEquals(HTTPRequest.POST, message.getMetadata().getRequest());

        JsonObject object = message.toJson();

        Assert.assertNull(object.getJsonObject(CoordinationMessage.MESSAGE_KEY));
        Assert.assertNotNull(object.getJsonObject(CoordinationMessage.METADATA_KEY));
        Assert.assertEquals(CandidateLeadershipInformationTest.JSON.encodePrettily(),
            object.getJsonObject(CoordinationMessage.DEFINITION_KEY).encodePrettily());
    }
}
