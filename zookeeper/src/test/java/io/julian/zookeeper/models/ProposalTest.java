package io.julian.zookeeper.models;

import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ProposalTest {
    public static final HTTPRequest METHOD = HTTPRequest.POST;
    public static final JsonObject MESSAGE = new JsonObject().put("message", "key");
    public static final String MESSAGE_ID = "random-id1234";

    public static final float EPOCH = ZxidTest.EPOCH;
    public static final float COUNTER = ZxidTest.COUNTER;
    public static final Zxid TRANSACTION_ID = new Zxid(EPOCH, COUNTER);

    public static final ClientMessage CLIENT_MESSAGE = new ClientMessage(METHOD, MESSAGE, MESSAGE_ID);

    public static final JsonObject JSON = new JsonObject()
        .put(Proposal.NEW_STATE_KEY, CLIENT_MESSAGE.toJson())
        .put(Proposal.TRANSACTION_ID_KEY, TRANSACTION_ID.toJson());

    @Test
    public void TestInitialization() {
        Proposal proposal = new Proposal(CLIENT_MESSAGE, TRANSACTION_ID);

        Assert.assertEquals(COUNTER, proposal.getTransactionId().getCounter(), 0);
        Assert.assertEquals(EPOCH, proposal.getTransactionId().getEpoch(), 0);

        Assert.assertEquals(METHOD, proposal.getNewState().getRequest());
        Assert.assertEquals(MESSAGE.encodePrettily(), proposal.getNewState().getMessage().encodePrettily());
        Assert.assertEquals(MESSAGE_ID, proposal.getNewState().getMessageId());

        Assert.assertEquals(JSON.encodePrettily(), proposal.toJson().encodePrettily());
    }

    @Test
    public void TestMapFrom() {
        Proposal proposal = Proposal.mapFrom(JSON);
        Assert.assertEquals(COUNTER, proposal.getTransactionId().getCounter(), 0);
        Assert.assertEquals(EPOCH, proposal.getTransactionId().getEpoch(), 0);

        Assert.assertEquals(METHOD, proposal.getNewState().getRequest());
        Assert.assertEquals(MESSAGE.encodePrettily(), proposal.getNewState().getMessage().encodePrettily());
        Assert.assertEquals(MESSAGE_ID, proposal.getNewState().getMessageId());
    }
}
