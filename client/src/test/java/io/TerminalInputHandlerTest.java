package io;

import io.julian.client.io.InputReader;
import io.julian.client.io.TerminalInputHandler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TerminalInputHandlerTest {
    private final InputReader inputReader = mock(InputReader.class);
    private final TerminalInputHandler input = new TerminalInputHandler(inputReader);

    @Test
    public void TestTerminalInputHandlerCanConvertToNumber() throws NumberFormatException {
        when(inputReader.nextLine())
            .thenReturn(" 1 ")
            .thenReturn("1");
        Assert.assertEquals(1, input.getNumberFromInput());
        Assert.assertEquals(1, input.getNumberFromInput());
    }

    @Test
    public void TestTerminalInputHandlerThrowsNFE() {
        when(inputReader.nextLine())
            .thenReturn("Not A Number ");
        try {
            input.getNumberFromInput();
            Assert.fail();
        } catch (NumberFormatException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void TestTerminalInputHandlerCanReadString() {
        String expected = "wow";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", expected))
            .thenReturn(expected);

        Assert.assertEquals(expected, input.getStringFromInput());
        Assert.assertEquals(expected, input.getStringFromInput());
    }

    @Test
    public void TestTerminalInputHandlerCanCreateJsonObject() throws DecodeException {
        JsonObject message = new JsonObject().put("really", "works");
        String value = "{\"really\":\"works\"}";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", value))
            .thenReturn(value);

        Assert.assertEquals(message, input.getJsonObjectFromInput());
        Assert.assertEquals(message, input.getJsonObjectFromInput());
    }

    @Test
    public void TestTerminalInputHandlerHandlesInvalidJsonWhenCreatingJsonObject() {
        String invalidJsonString = "{dfd;";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", invalidJsonString));

        try {
            input.getJsonObjectFromInput();
            Assert.fail();
        } catch (DecodeException e) {
            Assert.assertNotNull(e);
        }
    }
}
