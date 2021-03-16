package io.julian.zookeeper.models;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ZxidTest {
    public static final int EPOCH = 32;
    public static final int COUNTER = 1234;
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

    @Test
    public void TestEquals() {
        Zxid first = JSON.mapTo(Zxid.class);
        Zxid second = JSON.mapTo(Zxid.class);

        Assert.assertEquals(first, second);
    }

    @Test
    public void TestHashToSame() {
        Zxid first = JSON.mapTo(Zxid.class);
        Zxid second = JSON.mapTo(Zxid.class);
        HashSet<Zxid> set = new HashSet<>();

        set.add(first);
        Assert.assertTrue(set.contains(first));
        Assert.assertTrue(set.contains(second));
    }

    @Test
    public void TestIsLaterThan() {
        Zxid highest = JSON.mapTo(Zxid.class);
        Zxid lowest = new Zxid(0, 0);
        Zxid middle = new Zxid(EPOCH - 1, COUNTER + 10);

        Assert.assertTrue(highest.isLaterThan(lowest));
        Assert.assertTrue(highest.isLaterThan(middle));
        Assert.assertTrue(middle.isLaterThan(lowest));

        Assert.assertFalse(middle.isLaterThan(highest));
        Assert.assertFalse(lowest.isLaterThan(middle));
        Assert.assertFalse(lowest.isLaterThan(highest));
    }

    @Test
    public void TestComparatorCanSort() {
        Zxid highest = JSON.mapTo(Zxid.class);
        Zxid lowest = new Zxid(0, 0);
        Zxid middle = new Zxid(EPOCH - 1, COUNTER + 10);

        List<Zxid> list = Arrays.asList(highest, lowest, middle);
        list.sort(Zxid::comparator);

        boolean isSorted = true;
        for (int i = 1; i < list.size(); i++) {
            if (!list.get(i).isLaterThan(list.get(i - 1))) {
                isSorted = false;
            }
        }
        Assert.assertTrue(isSorted);
    }

    @Test
    public void TestToString() {
        Zxid first = JSON.mapTo(Zxid.class);
        Assert.assertEquals("(epoch: " + ZxidTest.EPOCH + ", counter: " + ZxidTest.COUNTER + ")", first.toString());
    }
}
