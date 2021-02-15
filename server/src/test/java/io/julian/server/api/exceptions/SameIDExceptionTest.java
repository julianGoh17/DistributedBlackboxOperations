package io.julian.server.api.exceptions;

import org.junit.Assert;
import org.junit.Test;

public class SameIDExceptionTest {
    @Test
    public void TestGetterAndToString() {
        String uuid = "any-uuid-1234@";
        SameIDException exception = new SameIDException(uuid);

        Assert.assertEquals(uuid, exception.getId());
        Assert.assertEquals("Server already contains message with id '" + uuid + "'",
            exception.toString());
    }
}
