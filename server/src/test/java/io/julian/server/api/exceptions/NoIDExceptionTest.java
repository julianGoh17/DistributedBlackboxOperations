package io.julian.server.api.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class NoIDExceptionTest {
    @Test
    public void TestGetterAndToString() {
        String uuid = "any-uuid-1234@";
        NoIDException exception = new NoIDException(uuid);

        Assert.assertEquals(uuid, exception.getId());
        Assert.assertEquals("Server does not contain message with id '" + uuid + "'",
            exception.toString());
    }
}
