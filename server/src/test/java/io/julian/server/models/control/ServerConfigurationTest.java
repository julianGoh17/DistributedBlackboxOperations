package io.julian.server.models.control;

import org.junit.Assert;
import org.junit.Test;

public class ServerConfigurationTest {

    @Test
    public void TestOtherServerConfigurationGetterAndSetter() {
        int originalPort = -123;
        int newPort = 1234;

        String originalHost = "original";
        String newHost = "new";

        String newLabel = "label";

        ServerConfiguration config = new ServerConfiguration(originalHost, originalPort);
        Assert.assertEquals(originalHost, config.getHost());
        Assert.assertEquals(originalPort, config.getPort());
        Assert.assertNull(config.getLabel());
        Assert.assertNotEquals(newHost, config.getHost());
        Assert.assertNotEquals(newPort, config.getPort());
        Assert.assertNotEquals(newLabel, config.getLabel());

        config.setHost(newHost);
        config.setPort(newPort);
        config.setLabel(newLabel);
        Assert.assertNotEquals(originalHost, config.getHost());
        Assert.assertNotEquals(originalPort, config.getPort());
        Assert.assertEquals(newHost, config.getHost());
        Assert.assertEquals(newPort, config.getPort());
        Assert.assertEquals(newLabel, config.getLabel());
    }
}
