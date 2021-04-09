package io.julian.client;

import io.julian.client.io.InputReader;
import io.julian.client.io.OutputPrinter;
import io.julian.client.io.TerminalInputHandler;
import io.julian.client.io.TerminalOutputHandler;
import io.julian.client.metrics.Reporter;
import io.julian.client.operations.ClientConfiguration;
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
        final ClientConfiguration clientConfiguration = new ClientConfiguration();
        final Coordinator coordinator = new Coordinator(vertx, clientConfiguration);
        final Reporter reporter = new Reporter();
        TerminalInputHandler input = new TerminalInputHandler(READER, vertx);
        final Controller controller = new Controller(input, OUTPUT, coordinator, vertx);
        initialize(coordinator, reporter, clientConfiguration);
        controller.run(clientConfiguration.getReportFilePath())
            .onComplete(res -> {
                if (res.succeeded()) {
                    System.out.printf("Successfully created report at '%s/%s'%n", clientConfiguration.getReportFilePath(), REPORT_FILE_NAME);
                } else {
                    System.out.println(res.cause().getMessage());
                }
                controller.close();
                vertx.close();
            });
    }

    public static void initialize(final Coordinator coordinator, final Reporter reporter, final ClientConfiguration clientConfiguration) throws IOException, NullPointerException {
        log.traceEntry(() -> coordinator, () -> reporter, () -> clientConfiguration);
        coordinator.initialize(clientConfiguration.getMessageFilePath(), clientConfiguration.getOperationsFilePath());
        reporter.checkReportFolderExists(clientConfiguration.getReportFilePath());
        log.traceExit();
    }
}
