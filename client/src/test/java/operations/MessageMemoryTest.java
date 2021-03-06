package operations;

import io.julian.client.operations.MessageMemory;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MessageMemoryTest {
    private static final String TEST_MESSAGE_FILES_PATH = String.format("%s/src/test/resources/messages", System.getProperty("user.dir"));

    @Test
    public void TestMessageMemoryInitializedWithMessages() throws IOException {
        MessageMemory memory = new MessageMemory();
        memory.readInMessageFiles(TEST_MESSAGE_FILES_PATH);
        File folder = new File(TEST_MESSAGE_FILES_PATH);
        Assert.assertEquals(folder.listFiles().length, memory.getOriginalMessages().size());
        int i = 0;
        for (File file : folder.listFiles()) {
            JsonObject message = new JsonObject(FileUtils.readFileToString(file));
            Assert.assertEquals(message, memory.getOriginalMessage(i));
            i++;
        }
        Assert.assertEquals(0, memory.getExpectedMapping().size());
    }

    @Test
    public void TestPutAndGetExpectedMemory() {
        MessageMemory memory = new MessageMemory();
        String id = "my-id";
        String newID = "new-id";
        int num = 0;

        memory.associateNumberWithID(num, id);
        Assert.assertEquals(id, memory.getExpectedIDForNum(num));

        memory.associateNumberWithID(num, newID);
        Assert.assertNotEquals(id, memory.getExpectedIDForNum(num));
        Assert.assertEquals(newID, memory.getExpectedIDForNum(num));
    }

    @Test
    public void TestCanRemoveKeysFromExpectedMemory() {
        MessageMemory memory = new MessageMemory();
        String id = "my-id";
        int num = 0;

        memory.associateNumberWithID(num, id);
        Assert.assertEquals(id, memory.getExpectedIDForNum(num));

        memory.disassociateNumberFromID(num);
        Assert.assertNull(memory.getExpectedIDForNum(num));

        memory.disassociateNumberFromID(num);
        Assert.assertNull(memory.getExpectedIDForNum(num));
    }

    @Test
    public void TestGetNullKeyExpectedMemory() {
        MessageMemory memory = new MessageMemory();
        Assert.assertNull(memory.getExpectedIDForNum(0));
    }

    @Test
    public void TestMessageMemoryThrowsNPEWhenIncorrectPath() throws IOException {
        MessageMemory memory = new MessageMemory();
        try {
            memory.readInMessageFiles(String.format("%s/random-1234124", TEST_MESSAGE_FILES_PATH));
            Assert.fail();
        } catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }
}
