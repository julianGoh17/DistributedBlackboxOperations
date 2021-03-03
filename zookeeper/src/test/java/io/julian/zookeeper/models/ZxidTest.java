package io.julian.zookeeper.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ZxidTest {
    public static final float EPOCH = 32;
    public static final float COUNTER = 1234;
    public static final JsonObject JSON = new JsonObject().put(Zxid.EPOCH_KEY, EPOCH).put(Zxid.COUNTER_KEY, COUNTER);

    @Test
    public void TestInitialization() {
        Zxid id = new Zxid(EPOCH, COUNTER);
        Assert.assertEquals(EPOCH, id.getEpoch(), 0);
        Assert.assertEquals(COUNTER, id.getCounter(), 0);
        Assert.assertEquals(JSON.encodePrettily(), id.toJson().encodePrettily());
    }

    @Test
    public void TestMapTo() {
        Zxid id = JSON.mapTo(Zxid.class);
        Assert.assertEquals(EPOCH, id.getEpoch(), 0);
        Assert.assertEquals(COUNTER, id.getCounter(), 0);
    }

    @Test
    public void TestSetters() {
        Zxid id = JSON.mapTo(Zxid.class);
        Assert.assertEquals(EPOCH, id.getEpoch(), 0);
        Assert.assertEquals(COUNTER, id.getCounter(), 0);

        int offset = 100;
        id.setCounter(COUNTER + offset);
        id.setEpoch(EPOCH + offset);

        Assert.assertEquals(EPOCH + offset, id.getEpoch(), 0);
        Assert.assertEquals(COUNTER + offset, id.getCounter(), 0);
    }
}
