package io.julian.server.components;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {
    @Test
    public void TestInit() {
        Configuration configuration = new Configuration();
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, configuration.getServerPort());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, configuration.getServerHost());
        Assert.assertEquals(Configuration.DEFAULT_OPENAPI_SPEC_LOCATION, configuration.getOpenapiSpecLocation());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_CONFIGURATION_LOCATION, configuration.getServerConfigurationLocation());
        Assert.assertEquals("", configuration.getJarFilePath());
        Assert.assertEquals("", configuration.getPackageName());
        Assert.assertEquals(Configuration.DEFAULT_DOES_PROCESS_REQUEST, configuration.doesProcessRequest());
    }

    @Test
    public void TestSetter() {
        Configuration configuration = new Configuration();
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, configuration.getServerPort());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, configuration.getServerHost());
        Assert.assertEquals(Configuration.DEFAULT_OPENAPI_SPEC_LOCATION, configuration.getOpenapiSpecLocation());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_CONFIGURATION_LOCATION, configuration.getServerConfigurationLocation());
        Assert.assertEquals("", configuration.getJarFilePath());
        Assert.assertEquals("", configuration.getPackageName());

        String host = "new-host";
        int port = 93423;
        String openApiPath = "random/path";
        String serverConfigurationPath = "server/config/path";
        String jarFilePath = "jar.file";
        String packageName = "io.package";
        boolean doesProcessRequest = false;

        configuration.setServerHost(host);
        configuration.setServerPort(port);
        configuration.setOpenapiSpecLocation(openApiPath);
        configuration.setServerConfigurationLocation(serverConfigurationPath);
        configuration.setJarFilePath(jarFilePath);
        configuration.setPackageName(packageName);
        configuration.setDoesProcessRequest(doesProcessRequest);

        Assert.assertEquals(port, configuration.getServerPort());
        Assert.assertEquals(host, configuration.getServerHost());
        Assert.assertEquals(openApiPath, configuration.getOpenapiSpecLocation());
        Assert.assertEquals(serverConfigurationPath, configuration.getServerConfigurationLocation());
        Assert.assertEquals(jarFilePath, configuration.getJarFilePath());
        Assert.assertEquals(packageName, configuration.getPackageName());
        Assert.assertEquals(doesProcessRequest, configuration.doesProcessRequest());
    }
}
