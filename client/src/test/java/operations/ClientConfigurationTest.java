package operations;

import io.julian.client.operations.ClientConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class ClientConfigurationTest {
    private static final String NON_EXISTENT_ENV = "NON_EXISTENT_ENV";
    private static final String DEFAULT_VALUE_STRING = "string";
    private static final int DEFAULT_VALUE_INT = 1;

    @Test
    public void TestGetOrDefaultString() {
        Assert.assertEquals(DEFAULT_VALUE_STRING, ClientConfiguration.getOrDefault(NON_EXISTENT_ENV, DEFAULT_VALUE_STRING));
        Assert.assertEquals(DEFAULT_VALUE_INT, ClientConfiguration.getOrDefault(NON_EXISTENT_ENV, DEFAULT_VALUE_INT));
    }

    @Test
    public void TestInit() {
        ClientConfiguration configuration = new ClientConfiguration();
        Assert.assertEquals(ClientConfiguration.DEFAULT_MESSAGE_FILE_PATH, configuration.getMessageFilePath());
        Assert.assertEquals(ClientConfiguration.DEFAULT_OPERATIONS_MESSAGE_FILE_PATH, configuration.getOperationsFilePath());
        Assert.assertEquals(ClientConfiguration.DEFAULT_REPORT_MESSAGE_FILE_PATH, configuration.getReportFilePath());
        Assert.assertEquals(ClientConfiguration.DEFAULT_SERVER_HOSTS_FILE_PATH_ENV, configuration.getServerHostsFilePath());
        Assert.assertEquals(ClientConfiguration.DEFAULT_SERVER_HOST, configuration.getServerHost());
        Assert.assertEquals(ClientConfiguration.DEFAULT_SERVER_PORT, configuration.getServerPort());
    }
}
