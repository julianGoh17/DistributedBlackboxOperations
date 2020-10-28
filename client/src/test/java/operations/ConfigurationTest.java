package operations;

import io.julian.client.operations.Configuration;
import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {
    private static final String NON_EXISTENT_ENV = "NON_EXISTENT_ENV";
    private static final String DEFAULT_VALUE_STRING = "string";
    private static final int DEFAULT_VALUE_INT = 1;

    @Test
    public void TestGetOrDefaultString() {
        Assert.assertEquals(DEFAULT_VALUE_STRING, Configuration.getOrDefault(NON_EXISTENT_ENV, DEFAULT_VALUE_STRING));
        Assert.assertEquals(DEFAULT_VALUE_INT, Configuration.getOrDefault(NON_EXISTENT_ENV, DEFAULT_VALUE_INT));
    }
}
