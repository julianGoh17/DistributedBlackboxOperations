package model.operation;

import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Configuration;
import io.julian.client.model.operation.OperationChain;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class OperationChainTest extends AbstractOperationModelTest {
    @Test
    public void TestOperationChainMapsCorrectly() throws IOException {
        JsonObject content = readTestFile();
        OperationChain chain = content.mapTo(OperationChain.class);
        Assert.assertEquals(3, chain.getOperations().size());
        Assert.assertEquals(RequestMethod.POST, chain.getOperations().get(0).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(0).getAction().getMessageNumber().intValue());
        Assert.assertEquals(200, chain.getOperations().get(0).getExpected().getStatusCode());
        Assert.assertNull(chain.getOperations().get(0).getExpected().getMessageNumber());

        Assert.assertEquals(RequestMethod.GET, chain.getOperations().get(1).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(1).getAction().getMessageNumber().intValue());
        Assert.assertEquals(404, chain.getOperations().get(1).getExpected().getStatusCode());
        Assert.assertNull(chain.getOperations().get(1).getExpected().getMessageNumber());

        Assert.assertEquals(RequestMethod.DELETE, chain.getOperations().get(2).getAction().getMethod());
        Assert.assertEquals(1, chain.getOperations().get(2).getAction().getMessageNumber().intValue());
        Assert.assertEquals(200, chain.getOperations().get(2).getExpected().getStatusCode());


        Assert.assertFalse(chain.getConfiguration().willRunInParallel());
    }

    @Test
    public void TestOperationChainFailsIfNoOperations() {
        JsonObject emptyObject = new JsonObject();
        try {
            emptyObject.mapTo(OperationChain.class);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains(String.format("Missing required creator property '%s'", OPERATIONS_KEY)));
        }
    }

    @Test
    public void TestOperationCanUpdateExpectedMessages() {
        OperationChain chain = new OperationChain(new ArrayList<>(), new Configuration());
        String id = "random-id";

        Assert.assertNull(chain.getExpectedMessageID(0));
        Assert.assertEquals(0, chain.getExpectedMessages().size());

        chain.updateExpectedMapping(1, id);
        Assert.assertEquals(1, chain.getExpectedMessages().size());
        Assert.assertEquals(id, chain.getExpectedMessageID(1));
    }
}
