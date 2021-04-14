package io.julian.server.models.control;

import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class ServerConfigurationTest {
    private final static String HOST = "host";
    private final static int PORT = 9999;
    private final static String LABEL = "label";
    private final static JsonObject JSON = new JsonObject()
        .put("host", HOST)
        .put("port", PORT)
        .put("label", LABEL);

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
    public void TestMapJsonMethods() {
        ServerConfiguration config = new ServerConfiguration(HOST, PORT, LABEL);
        Assert.assertEquals(JSON.encodePrettily(), config.toJson().encodePrettily());

        config = JSON.mapTo(ServerConfiguration.class);
        Assert.assertEquals(HOST, config.getHost());
        Assert.assertEquals(PORT, config.getPort());
        Assert.assertEquals(LABEL, config.getLabel());

        JsonObject noLabel = JSON.copy();
        noLabel.remove(ServerConfiguration.LABEL_KEY);
        config = noLabel.mapTo(ServerConfiguration.class);
        Assert.assertEquals(HOST, config.getHost());
        Assert.assertEquals(PORT, config.getPort());
        Assert.assertNull(config.getLabel());
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
