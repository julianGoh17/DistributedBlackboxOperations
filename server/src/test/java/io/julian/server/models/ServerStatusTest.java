package io.julian.server.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ServerStatusTest {
    @Test
    public void FromStringMapsValuesAppropriately() {
        Map<String, ServerStatus> appropriateMappings = new HashMap<>();
        appropriateMappings.put("available", ServerStatus.AVAILABLE);
        appropriateMappings.put("unreachable", ServerStatus.UNREACHABLE);
        appropriateMappings.put("unavailable", ServerStatus.UNAVAILABLE);
        appropriateMappings.put("random", ServerStatus.UNKNOWN);
        appropriateMappings.put("unknown", ServerStatus.UNKNOWN);

        for (final String str : appropriateMappings.keySet()) {
            Assert.assertEquals(appropriateMappings.get(str), ServerStatus.forValue(str));
        }
    }

    @Test
    public void FromStringMapsConvertsToStringAppropriately() {
        Map<ServerStatus, String> appropriateMappings = new HashMap<>();
        appropriateMappings.put(ServerStatus.AVAILABLE, "available");
        appropriateMappings.put(ServerStatus.UNREACHABLE, "unreachable");
        appropriateMappings.put(ServerStatus.UNAVAILABLE, "unavailable");
        appropriateMappings.put(ServerStatus.UNKNOWN, "unknown");

        for (final ServerStatus status : appropriateMappings.keySet()) {
            Assert.assertEquals(appropriateMappings.get(status), status.toValue());
        }
    }
}
