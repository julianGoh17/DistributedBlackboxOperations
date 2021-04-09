package operations;

import io.julian.client.operations.ServerFileReader;
import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

public class ServerFileReaderTest {
    public final static String SERVER_FILE_PATH = String.format("%s/src/test/resources/serverFilePath.txt", System.getProperty("user.dir"));
    private final static String HOST = Configuration.DEFAULT_SERVER_HOST;
    private final static int PORT = Configuration.DEFAULT_SERVER_PORT;

    @Test
    public void TestCreateServerConfigurationIsSuccessful() {
        ServerConfiguration configuration = ServerFileReader.createServerConfiguration(String.format("%s:%d", HOST, PORT));
        Assert.assertNotNull(configuration);
        Assert.assertEquals(PORT, configuration.getPort());
        Assert.assertEquals(HOST, configuration.getHost());
    }

    @Test
    public void TestCreateServerConfigurationFailsWhenGivenImproperFormat() {
        ServerConfiguration configuration = ServerFileReader.createServerConfiguration(String.format("%s", HOST));
        Assert.assertNull(configuration);

        configuration = ServerFileReader.createServerConfiguration(String.format("%s:string", HOST));
        Assert.assertNull(configuration);
    }

    @Test
    public void TestReadServerFileThrowsFileNotFoundException() {
        String invalidPath = "/invalid/path";
        try {
            ServerFileReader.readServerFile(invalidPath);
            Assert.fail();
        } catch (final FileNotFoundException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(String.format("%s (No such file or directory)", invalidPath), e.getMessage());
        }
    }

    @Test
    public void TestReadServerFileSuccessfully() {
        try {
            List<ServerConfiguration> configurations = ServerFileReader.readServerFile(SERVER_FILE_PATH);
            Assert.assertEquals(1, configurations.size());
            Assert.assertEquals(HOST, configurations.get(0).getHost());
            Assert.assertEquals(PORT, configurations.get(0).getPort());
        } catch (final FileNotFoundException e) {
            Assert.fail();
        }
    }
}
