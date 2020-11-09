package io.julian.client;

import io.julian.client.io.InputReader;
import io.julian.client.io.OutputPrinter;
import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.metrics.Reporter;
import io.julian.client.operations.Configuration;
import io.julian.client.operations.Controller;
import io.julian.client.operations.Coordinator;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static io.julian.client.metrics.Reporter.REPORT_FILE_NAME;

public class Main {
    private final static Logger log = LogManager.getLogger(Main.class.getName());
    private final static InputReader READER = new InputReader();
    private final static OutputPrinter WRITER = new OutputPrinter();

    private final static TerminalOutputHandler OUTPUT = new TerminalOutputHandler(WRITER);

    public static void main(final String[] args) throws IOException, NullPointerException {
        final Vertx vertx = Vertx.vertx();
        final Coordinator coordinator = new Coordinator(vertx);
        final Reporter reporter = new Reporter();
        TerminalInputHandler input = new TerminalInputHandler(READER, vertx);
        final Controller controller = new Controller(input, OUTPUT, coordinator, vertx);
        initialize(coordinator, reporter);
        controller.run(Configuration.getReportFilePath())
            .onComplete(res -> {
                if (res.succeeded()) {
                    System.out.printf("Successfully created report at '%s/%s'%n", Configuration.getReportFilePath(), REPORT_FILE_NAME);
                } else {
                    System.out.println(res.cause().getMessage());
                }
                controller.close();
                vertx.close();
            });
    }

    public static void initialize(final Coordinator coordinator, final Reporter reporter) throws IOException, NullPointerException {
        log.traceEntry(() -> coordinator, () -> reporter);
        coordinator.initialize(Configuration.getMessageFilePath(), Configuration.getOperationsFilePath());
        reporter.checkReportFolderExists(Configuration.getReportFilePath());
        log.traceExit();
    }
}
