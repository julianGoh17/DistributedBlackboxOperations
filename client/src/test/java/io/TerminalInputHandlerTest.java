package io;

import io.julian.client.io.InputReader;
import io.julian.client.io.TerminalInputHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class TerminalInputHandlerTest {
    private final InputReader inputReader = mock(InputReader.class);
    private TerminalInputHandler input;
    private Vertx vertx;

    @Before
    public void Before() {
        vertx = Vertx.vertx();
        input = new TerminalInputHandler(inputReader, vertx);
    }

    @Test
    public void TestTerminalInputHandlerCanConvertToNumber(final TestContext context) throws NumberFormatException {
        when(inputReader.nextLine())
            .thenReturn(" 1 ")
            .thenReturn("1");
        input.getNumberFromInput()
            .onComplete(context.asyncAssertSuccess(firstNum -> {
                Assert.assertEquals(1, firstNum.intValue());
                input.getNumberFromInput()
                    .onComplete(context.asyncAssertSuccess(secondNum -> Assert.assertEquals(1, secondNum.intValue())));
            }));
    }

    @Test
    public void TestTerminalInputHandlerThrowsNFE(final TestContext context) {
        when(inputReader.nextLine())
            .thenReturn("Not A Number ");
        input.getNumberFromInput().onComplete(context.asyncAssertFailure(Assert::assertNotNull));
    }

    @Test
    public void TestTerminalInputHandlerCanReadString(final TestContext context) {
        String expected = "wow";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", expected))
            .thenReturn(expected);

        input.getStringFromInput().onComplete(context.asyncAssertSuccess(firstString -> {
            Assert.assertEquals(expected, firstString);
            input.getStringFromInput().onComplete(context.asyncAssertSuccess(secondString -> Assert.assertEquals(expected, secondString)));
        }));
    }

    @Test
    public void TestTerminalInputHandlerCanCreateJsonObject(final TestContext context) throws DecodeException {
        JsonObject message = new JsonObject().put("really", "works");
        String value = "{\"really\":\"works\"}";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", value))
            .thenReturn(value);
        input.getJsonObjectFromInput().onComplete(context.asyncAssertSuccess(firstJson -> {
            Assert.assertEquals(message, firstJson);
            input.getJsonObjectFromInput().onComplete(context.asyncAssertSuccess(secondJson -> Assert.assertEquals(message, secondJson)));
        }));
    }

    @Test
    public void TestTerminalInputHandlerHandlesInvalidJsonWhenCreatingJsonObject(final TestContext context) {
        String invalidJsonString = "{dfd;";
        when(inputReader.nextLine())
            .thenReturn(String.format("  %s  ", invalidJsonString));

        input.getJsonObjectFromInput().onComplete(context.asyncAssertFailure(Assert::assertNotNull));
    }
}
