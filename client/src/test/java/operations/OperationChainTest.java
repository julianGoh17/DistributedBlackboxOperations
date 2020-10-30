package operations;

import io.julian.client.model.RequestMethod;
import io.julian.client.operations.OperationChain;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class OperationChainTest {
    private static final String TEST_OPERATION_FILES_PATH = String.format("%s/src/test/resources/operations", System.getProperty("user.dir"));

    @Test
    public void TestOperationChainCanMapFromFile() throws IOException {
        File folder = new File(TEST_OPERATION_FILES_PATH);
        OperationChain chain = new OperationChain();
        for (final File file : folder.listFiles()) {
            if (file.getName().equals("test-example.json")) {
                chain.readInOperationFiles(file);
            }
        }

        Assert.assertEquals(0, chain.getExpectedMessages().size());

        Assert.assertEquals(3, chain.getOperations().size());
        Assert.assertEquals(RequestMethod.POST, chain.getOperations().get(0).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(0).getAction().getMessageNumber().intValue());
        Assert.assertEquals(200, chain.getOperations().get(0).getExpected().getStatusCode());
        Assert.assertNull(chain.getOperations().get(0).getExpected().getMessageNumber());

        Assert.assertEquals(RequestMethod.GET, chain.getOperations().get(1).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(1).getAction().getMessageNumber().intValue());
        Assert.assertEquals(404, chain.getOperations().get(1).getExpected().getStatusCode());
        Assert.assertNull(chain.getOperations().get(0).getExpected().getMessageNumber());

        Assert.assertEquals(RequestMethod.PUT, chain.getOperations().get(2).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(2).getAction().getMessageNumber().intValue());
        Assert.assertEquals(2, chain.getOperations().get(2).getAction().getNewMessageNumber().intValue());
        Assert.assertEquals(200, chain.getOperations().get(2).getExpected().getStatusCode());
        Assert.assertEquals(1, chain.getOperations().get(2).getExpected().getMessageNumber().intValue());
    }

    @Test
    public void TestOperationCanUpdateExpectedMessages() {
        OperationChain chain = new OperationChain();
        String id = "random-id";

        Assert.assertNull(chain.getExpectedMessageID(0));
        Assert.assertEquals(0, chain.getExpectedMessages().size());

        chain.updateExpectedMapping(1, id);
        Assert.assertEquals(1, chain.getExpectedMessages().size());
        Assert.assertEquals(id, chain.getExpectedMessageID(1));
    }
}
