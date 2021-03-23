package io.julian.server.api.client;

import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class RegistryManagerTest {
    public static final ServerConfiguration NO_LABEL_CONFIGURATION = new ServerConfiguration("host", 9999);
    public static final ServerConfiguration LABEL_CONFIGURATION = new ServerConfiguration("new-host", -134, "label");

    @Test
    public void TestRegistryMangerInit() {
        RegistryManager registryManager = createTestRegistryManger();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());
    }

    @Test
    public void TestRegistryManagerWithServersLoaded() {
        Configuration configuration = new Configuration();
        configuration.setServerConfigurationLocation(ServerConfigReaderTest.VALID_CONFIGURATION_PATH);
        RegistryManager registryManager = createTestRegistryManager(configuration);

        Assert.assertEquals(2, registryManager.getOtherServers().size());
    }

    @Test
    public void TestRegistryManagerDoesNotAddCurrentConfiguration() {
        Configuration configuration = new Configuration();
        configuration.setServerConfigurationLocation(ServerConfigReaderTest.VALID_CONFIGURATION_PATH);
        configuration.setServerPort(9898);
        RegistryManager registryManager = createTestRegistryManager(configuration);

        Assert.assertEquals(1, registryManager.getOtherServers().size());
    }

    @Test
    public void TestRegistryMangerAdd() {
        RegistryManager registryManager = createTestRegistryManger();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());

        registryManager.registerServer(NO_LABEL_CONFIGURATION.getHost(), NO_LABEL_CONFIGURATION.getPort());
        Assert.assertEquals(1, registryManager.getOtherServers().size());
        Assert.assertEquals(NO_LABEL_CONFIGURATION.getHost(), registryManager.getOtherServers().get(0).getHost());
    }

    @Test
    public void TestRegistryMangerFindServerWithLabel() {
        RegistryManager registryManager = createTestRegistryManger();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());

        registryManager.registerServer(NO_LABEL_CONFIGURATION.getHost(), NO_LABEL_CONFIGURATION.getPort());
        registryManager.registerServerWithLabel(LABEL_CONFIGURATION.getHost(), LABEL_CONFIGURATION.getPort(), LABEL_CONFIGURATION.getLabel());

        Assert.assertEquals(2, registryManager.getOtherServers().size());
        Assert.assertEquals(1, registryManager.getOtherServersWithLabel(LABEL_CONFIGURATION.getLabel()).size());
        Assert.assertEquals(LABEL_CONFIGURATION.getHost(), registryManager.getOtherServersWithLabel(LABEL_CONFIGURATION.getLabel()).get(0).getHost());
    }

    private RegistryManager createTestRegistryManger() {
        return createTestRegistryManager(new Configuration());
    }

    private RegistryManager createTestRegistryManager(final Configuration configuration) {
        return new RegistryManager(configuration);
    }
}
