package io.julian.server.client;

import io.julian.server.models.control.OtherServerConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class RegistryManagerTest {
    public static final OtherServerConfiguration NO_LABEL_CONFIGURATION = new OtherServerConfiguration("host", 9999);
    public static final OtherServerConfiguration LABEL_CONFIGURATION = new OtherServerConfiguration("new-host", -134, "label");

    @Test
    public void TestRegistryMangerInit() {
        RegistryManager registryManager = new RegistryManager();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());
    }

    @Test
    public void TestRegistryMangerAdd() {
        RegistryManager registryManager = new RegistryManager();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());

        registryManager.registerServer(NO_LABEL_CONFIGURATION.getHost(), NO_LABEL_CONFIGURATION.getPort());
        Assert.assertEquals(1, registryManager.getOtherServers().size());
        Assert.assertEquals(NO_LABEL_CONFIGURATION.getHost(), registryManager.getOtherServers().get(0).getHost());
    }

    @Test
    public void TestRegistryMangerFindServerWithLabel() {
        RegistryManager registryManager = new RegistryManager();
        Assert.assertNotNull(registryManager.getOtherServers());
        Assert.assertEquals(0, registryManager.getOtherServers().size());

        registryManager.registerServer(NO_LABEL_CONFIGURATION.getHost(), NO_LABEL_CONFIGURATION.getPort());
        registryManager.registerServerWithLabel(LABEL_CONFIGURATION.getHost(), LABEL_CONFIGURATION.getPort(), LABEL_CONFIGURATION.getLabel());

        Assert.assertEquals(2, registryManager.getOtherServers().size());
        Assert.assertEquals(1, registryManager.getOtherServersWithLabel(LABEL_CONFIGURATION.getLabel()).size());
        Assert.assertEquals(LABEL_CONFIGURATION.getHost(), registryManager.getOtherServersWithLabel(LABEL_CONFIGURATION.getLabel()).get(0).getHost());
    }
}
