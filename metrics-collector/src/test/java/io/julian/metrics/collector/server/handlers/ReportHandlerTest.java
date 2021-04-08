package io.julian.metrics.collector.server.handlers;

import io.julian.metrics.collector.TestClient;
import io.julian.metrics.collector.TestServerComponents;
import io.julian.metrics.collector.report.ReportCreator;
import io.julian.metrics.collector.report.ReportCreatorTest;
import io.julian.metrics.collector.server.Configuration;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class ReportHandlerTest extends AbstractHandlerTest {
    @Test
    public void TestCreateReportIsSuccessful(final TestContext context) {
        TestServerComponents server = startServer(context);
        TestClient client = createTestClient();

        client.successfulCreateReport(context, ReportCreator.GENERAL_STATISTIC_FILTER_NAME);
        ReportCreatorTest.assertReportFileExists(vertx, context, true);
        ReportCreatorTest.deleteReportFile(vertx, context);
        server.tearDownServer(context);
    }

    @Test
    public void TestCreateReportIsFails(final TestContext context) {
        Configuration configuration = new Configuration();
        configuration.setReportPath("invalid/path");
        TestServerComponents server = startServer(configuration, context);
        TestClient client = createTestClient();

        client.unsuccessfulCreateReport(context, ReportCreator.GENERAL_STATISTIC_FILTER_NAME,
            String.format("java.nio.file.NoSuchFileException: %s/%s", configuration.getReportPath(), ReportCreator.REPORT_FILE_NAME),
            403);
        ReportCreatorTest.assertReportFileExists(vertx, context, false);
        server.tearDownServer(context);
    }
}
