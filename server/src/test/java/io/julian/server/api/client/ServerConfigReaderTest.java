package io.julian.server.api.client;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class ServerConfigReaderTest {
    public final static String RESOURCES_FOLDER = String.format("%s/src/test/resources", System.getProperty("user.dir"));
    public final static String VALID_CONFIGURATION_PATH = String.format("%s/validServerConfiguration.txt", RESOURCES_FOLDER);
    public final static String NUMBER_FORMAT_EXCEPTION_PATH = String.format("%s/NumberFormatExceptionConfiguration.txt", RESOURCES_FOLDER);

    @Test
    public void TestReadFileThrowsException() {
        ServerConfigReader reader = new ServerConfigReader();
        String invalidPath = "/invalid/path";
        try {
            reader.readFile(invalidPath);
            Assert.fail();
        } catch (final IOException e) {
            Assert.assertEquals(invalidPath, e.getMessage());
        }
    }

    @Test
    public void TestReadFilePasses() {
        ServerConfigReader reader = new ServerConfigReader();
        String expectedContents = "localhost:9898\nlocalhost:8989";
        try {
            String fileContents = reader.readFile(VALID_CONFIGURATION_PATH);
            Assert.assertEquals(expectedContents, fileContents);
        } catch (final IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void TestGetServerConfigurationsPasses() {
        ServerConfigReader reader = new ServerConfigReader();
        try {
            ArrayList<Pair<String, Integer>> config = reader.getServerConfigurations(VALID_CONFIGURATION_PATH);
            Assert.assertEquals(9898, config.get(0).getRight().intValue());
            Assert.assertEquals("localhost", config.get(0).getLeft());
            Assert.assertEquals(8989, config.get(1).getRight().intValue());
            Assert.assertEquals("localhost", config.get(1).getLeft());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void TestGetServerConfigurationsThrowsNumberFormatException() {
        ServerConfigReader reader = new ServerConfigReader();
        try {
            reader.getServerConfigurations(NUMBER_FORMAT_EXCEPTION_PATH);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals("For input string: \"string\"", e.getLocalizedMessage());
        }
    }
}
