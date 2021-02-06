package io.julian.server.models;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class HTTPRequestTest {
    @Test
    public void FromStringMapsValuesAppropriately() {
        Map<String, HTTPRequest> appropriateMappings = new HashMap<>();
        appropriateMappings.put("GET", HTTPRequest.GET);
        appropriateMappings.put("POST", HTTPRequest.POST);
        appropriateMappings.put("DELETE", HTTPRequest.DELETE);
        appropriateMappings.put("PUT", HTTPRequest.PUT);
        appropriateMappings.put("any", HTTPRequest.UNKNOWN);


        for (final String str : appropriateMappings.keySet()) {
            Assert.assertEquals(appropriateMappings.get(str), HTTPRequest.forValue(str));
        }
    }

    @Test
    public void FromStringMapsConvertsToStringAppropriately() {
        Map<HTTPRequest, String> appropriateMappings = new HashMap<>();
        appropriateMappings.put(HTTPRequest.GET, "GET");
        appropriateMappings.put(HTTPRequest.POST, "POST");
        appropriateMappings.put(HTTPRequest.DELETE, "DELETE");
        appropriateMappings.put(HTTPRequest.PUT, "PUT");
        appropriateMappings.put(HTTPRequest.UNKNOWN, "UNKNOWN");

        for (final HTTPRequest request : appropriateMappings.keySet()) {
            Assert.assertEquals(appropriateMappings.get(request), request.toValue());
        }
    }
}
