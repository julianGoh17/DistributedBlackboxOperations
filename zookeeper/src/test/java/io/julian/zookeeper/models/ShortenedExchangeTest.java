package io.julian.zookeeper.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ShortenedExchangeTest {
    public final static MessagePhase PHASE = MessagePhase.COMMIT;

    public final static JsonObject JSON = new JsonObject()
        .put(ShortenedExchange.PHASE_KEY, PHASE.toValue())
        .put(ShortenedExchange.TRANSACTIONAL_ID_KEY, ZxidTest.JSON);

    @Test
    public void TestFromJson() {
        ShortenedExchange exchange = JSON.mapTo(ShortenedExchange.class);
        Assert.assertEquals(PHASE, exchange.getPhase());
        Assert.assertEquals(ZxidTest.COUNTER, exchange.getTransactionID().getCounter(), 0);
        Assert.assertEquals(ZxidTest.EPOCH, exchange.getTransactionID().getEpoch(), 0);

        Assert.assertEquals(JSON.encodePrettily(), exchange.toJson().encodePrettily());
    }

    @Test
    public void TestSetters() {
        ShortenedExchange exchange = JSON.mapTo(ShortenedExchange.class);
        Assert.assertEquals(PHASE, exchange.getPhase());
        Assert.assertEquals(ZxidTest.COUNTER, exchange.getTransactionID().getCounter(), 0);
        Assert.assertEquals(ZxidTest.EPOCH, exchange.getTransactionID().getEpoch(), 0);

        Zxid id = ZxidTest.JSON.mapTo(Zxid.class);
        MessagePhase phase = MessagePhase.ACK;

        exchange.setPhase(phase);
        exchange.setTransactionID(id);

        Assert.assertEquals(phase, exchange.getPhase());
        Assert.assertEquals(id, exchange.getTransactionID());
    }
}
