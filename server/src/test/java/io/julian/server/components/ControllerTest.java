package io.julian.server.components;

import io.julian.server.models.ServerStatus;
import io.julian.server.models.control.ServerConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class ControllerTest {
    @Test
    public void TestControllerInitializesProperly() {
        Controller controller = new Controller();
        Assert.assertEquals(ServerStatus.AVAILABLE, controller.getStatus());
        Assert.assertEquals("", controller.getLabel());
    }

    @Test
    public void TestControllerCanSetStatus() {
        Controller controller = new Controller();
        controller.setStatus(ServerStatus.UNKNOWN);
        Assert.assertEquals(ServerStatus.UNKNOWN, controller.getStatus());
    }

    @Test
    public void TestControllerCanSetLabel() {
        String label = "random-label-123";
        Controller controller = new Controller();
        controller.setLabel(label);
        Assert.assertEquals(label, controller.getLabel());
    }

    @Test
    public void TestControllerGetServerConfiguration() {
        Controller controller = new Controller();
        ServerConfiguration configuration = controller.getServerConfiguration();
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, configuration.getHost());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, configuration.getPort());
    }
}
