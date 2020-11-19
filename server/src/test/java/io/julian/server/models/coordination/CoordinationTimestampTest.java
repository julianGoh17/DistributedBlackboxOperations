package io.julian.server.models.coordination;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class CoordinationTimestampTest {
    @Test
    public void TestCoordinationTimestampCanMapFromString() {
        LocalDateTime now = LocalDateTime.now();
        CoordinationTimestamp timestamp = CoordinationTimestamp.fromJson(now.toString());
        Assert.assertEquals(now, timestamp.toLocalDateTime());
    }

    @Test
    public void TestCoordinationTimestampCanMapToString() {
        LocalDateTime now = LocalDateTime.now();
        CoordinationTimestamp timestamp = new CoordinationTimestamp(now);
        Assert.assertEquals(now.toString(), timestamp.toValue());
    }
}
