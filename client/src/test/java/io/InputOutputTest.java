package io;

import io.julian.client.io.InputReader;
import io.julian.client.io.OutputPrinter;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InputOutputTest {
    private final InputReader inputReader = mock(InputReader.class);
    private final OutputPrinter outputPrinter = mock(OutputPrinter.class);

    private static class FakeCombinedReaderAndWriter {
        private final InputReader inputReader;
        private final OutputPrinter outputPrinter;

        public FakeCombinedReaderAndWriter(final InputReader inputReader, final OutputPrinter outputPrinter) {
            this.inputReader = inputReader;
            this.outputPrinter = outputPrinter;
        }

        public void readThenPrint() {
            outputPrinter.println(inputReader.nextLine() + " blah");
        }
    }

    @Test
    public void TestFakedInteractionWorks() {
        String first = "First";
        String second = "Second with some w3ird ch@r@ct3rs";
        FakeCombinedReaderAndWriter fake = new FakeCombinedReaderAndWriter(inputReader, outputPrinter);
        when(inputReader.nextLine())
            .thenReturn(first)
            .thenReturn(second);
        fake.readThenPrint();
        InOrder order = inOrder(inputReader, outputPrinter);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println(first + " blah");

        fake.readThenPrint();
        order = inOrder(inputReader, outputPrinter);
        order.verify(inputReader).nextLine();
        order.verify(outputPrinter).println(second + " blah");
    }
}
