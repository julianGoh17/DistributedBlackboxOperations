package io.julian.server.models.control;

import org.junit.Assert;
import org.junit.Test;

public class ServerConfigurationTest {
    private final static String HOST = "host";
    private final static int PORT = 9999;
    private final static String LABEL = "label";

    @Test
    public void TestOtherServerConfigurationGetterAndSetter() {
        String newHost = "new";
        int newPort = 1234;
        String newLabel = "label";

        ServerConfiguration config = new ServerConfiguration(HOST, PORT);
        Assert.assertEquals(HOST, config.getHost());
        Assert.assertEquals(PORT, config.getPort());
        Assert.assertNull(config.getLabel());
        Assert.assertNotEquals(newHost, config.getHost());
        Assert.assertNotEquals(newPort, config.getPort());
        Assert.assertNotEquals(newLabel, config.getLabel());

        config.setHost(newHost);
        config.setPort(newPort);
        config.setLabel(newLabel);
        Assert.assertNotEquals(HOST, config.getHost());
        Assert.assertNotEquals(PORT, config.getPort());
        Assert.assertEquals(newHost, config.getHost());
        Assert.assertEquals(newPort, config.getPort());
        Assert.assertEquals(newLabel, config.getLabel());
    }

    @Test
    public void TestIsEqual() {
        ServerConfiguration config = new ServerConfiguration(HOST, PORT, LABEL);
        ServerConfiguration otherConfiguration = new ServerConfiguration(HOST, PORT, LABEL);

        Assert.assertTrue(config.isEqual(otherConfiguration));
        otherConfiguration.setLabel("random-label");
        Assert.assertFalse(config.isEqual(otherConfiguration));

        otherConfiguration.setLabel(LABEL);
        otherConfiguration.setHost("Host-random1234513s");
        Assert.assertFalse(config.isEqual(otherConfiguration));

        otherConfiguration.setHost(HOST);
        otherConfiguration.setPort(92213);
        Assert.assertFalse(config.isEqual(otherConfiguration));
    }

    @Test
    public void TestIsEqualWhenNullLabel() {
        String label = "label";
        ServerConfiguration config = new ServerConfiguration(HOST, PORT);
        ServerConfiguration otherConfiguration = new ServerConfiguration(HOST, PORT);

        Assert.assertTrue(config.isEqual(otherConfiguration));
        otherConfiguration.setLabel(label);
        Assert.assertFalse(config.isEqual(otherConfiguration));

        otherConfiguration.setLabel(null);
        config.setLabel(label);
        Assert.assertFalse(config.isEqual(otherConfiguration));

        otherConfiguration.setLabel(label);
        Assert.assertTrue(config.isEqual(otherConfiguration));
    }

    @Test
    public void TestIsHostAndPortEqual() {
        ServerConfiguration config = new ServerConfiguration(HOST, PORT, LABEL);
        ServerConfiguration otherConfiguration = new ServerConfiguration(HOST, PORT, LABEL);

        Assert.assertTrue(config.isHostAndPortEqual(otherConfiguration));
        otherConfiguration.setLabel("random-label");
        Assert.assertTrue(config.isHostAndPortEqual(otherConfiguration));

        otherConfiguration.setHost("Host-random1234513s");
        Assert.assertFalse(config.isHostAndPortEqual(otherConfiguration));

        otherConfiguration.setHost(HOST);
        otherConfiguration.setPort(92213);
        Assert.assertFalse(config.isHostAndPortEqual(otherConfiguration));
    }
}
