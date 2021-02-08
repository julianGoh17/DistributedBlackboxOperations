package model.operation;

import io.julian.client.model.operation.Expected;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExpectedTest extends AbstractOperationModelTest {
    @Test
    public void TestExpectedMapsCorrectly() throws IOException {
        JsonObject testContent = readTestFile();
        JsonObject firstMessage = testContent.getJsonArray(OPERATIONS_KEY).getJsonObject(0).getJsonObject("expected");

        Expected expected = firstMessage.mapTo(Expected.class);
        Assert.assertEquals(200, expected.getStatusCode());
    }

    @Test
    public void TestExpectedMapsFromEmptyJson() {
        Expected action = new JsonObject().mapTo(Expected.class);
        Assert.assertEquals(0, action.getStatusCode());
    }

    @Test
    public void TestExpectedCorrectlyReturnsWhenStatusCodeUnequal() {
        int correctStatusCode = 30314;
        int incorrectStatusCode = -1;
        Expected expected = new Expected(correctStatusCode);

        Assert.assertTrue(expected.doesNotMatchExpectedStatusCode(incorrectStatusCode));
        Assert.assertFalse(expected.doesNotMatchExpectedStatusCode(correctStatusCode));
    }

    @Test
    public void TestExpectedCreatesServerErrorWhenStringPassedIn() {
        int correctStatusCode = 30314;
        int incorrectStatusCode = -1;
        Expected expected = new Expected(correctStatusCode);

        Assert.assertEquals(
            String.format(Expected.MISMATCHED_STATUS_CODE_ERROR_FORMAT, incorrectStatusCode, correctStatusCode) +
                String.format(Expected.SERVER_ERROR, "Server Error"),
            expected.generateMismatchedException(incorrectStatusCode, "Server Error").getMessage()
        );
    }

    @Test
    public void TestExpectedCreatesClientErrorWhenNoStringPassedIn() {
        int correctStatusCode = 30314;
        int incorrectStatusCode = -1;
        Expected expected = new Expected(correctStatusCode);

        Assert.assertEquals(
            String.format(Expected.MISMATCHED_STATUS_CODE_ERROR_FORMAT, incorrectStatusCode, correctStatusCode) +
               Expected.CLIENT_ERROR,
            expected.generateMismatchedException(incorrectStatusCode, null).getMessage()
        );
    }
}
