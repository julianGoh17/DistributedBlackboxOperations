package io.julian.metrics.collector.report;

import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.metrics.collector.report.filters.GenericStatistics;
import io.julian.metrics.collector.tracking.StatusTracker;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ReportCreatorTest {
    private Vertx vertx;
    public final static String REPORT_LOCATION = String.format("%s/src/test/resources", System.getProperty("user.dir"));
    public final static String REPORT_FILE_PATH = String.format("%s/%s", REPORT_LOCATION, ReportCreator.REPORT_FILE_NAME);

    private final static String MESSAGE_ID = "random-id";
    private final static TrackedMessage MESSAGE = new TrackedMessage(200, MESSAGE_ID, 10.5f);

    @Before
    public void before() {
        vertx = Vertx.vertx();
    }

    @After
    public void after() {
        vertx.close();
    }

    @Test
    public void TestCreateReport() {
        StatusTracker tracker = new StatusTracker();
        ReportCreator creator = new ReportCreator(tracker, vertx);
        tracker.updateStatus(MESSAGE);
        String expectedReport = MESSAGE_ID + "\n" +
            "-".repeat(MESSAGE_ID.length()) + "\n" +
            String.format("%s: 10.50 bytes\n", GenericStatistics.AVERAGE_MESSAGE_SIZE_KEY) +
            String.format("%s: 1\n", GenericStatistics.TOTAL_MESSAGES_KEY) +
            String.format("%s: 0\n", GenericStatistics.TOTAL_FAILED_MESSAGES_KEY) +
            String.format("%s: 1\n", GenericStatistics.TOTAL_SUCCEEDED_MESSAGES_KEY) +
            String.format("%s: 0 minutes:0.0 seconds\n", GenericStatistics.TOTAL_TIME_KEY) +
            "\n";

        Assert.assertEquals(expectedReport, creator.createReport(ReportCreator.GENERAL_STATISTIC_FILTER_NAME));
    }

    @Test
    public void TestCreateReportFileFails(final TestContext context) {
        StatusTracker tracker = new StatusTracker();
        ReportCreator creator = new ReportCreator(tracker, vertx);

        String invalidLocation = "/incorrect/location";
        Async async = context.async();
        creator.createReportFile(ReportCreator.GENERAL_STATISTIC_FILTER_NAME, invalidLocation)
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(String.format("java.nio.file.NoSuchFileException: %s/%s", invalidLocation, ReportCreator.REPORT_FILE_NAME), cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestCreateReportFileSucceeds(final TestContext context) {
        StatusTracker tracker = new StatusTracker();
        ReportCreator creator = new ReportCreator(tracker, vertx);

        Async async = context.async();
        creator.createReportFile(ReportCreator.GENERAL_STATISTIC_FILTER_NAME, REPORT_LOCATION)
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        assertReportFileExists(vertx, context, true);
        deleteReportFile(vertx, context);
    }

    public static void assertReportFileExists(final Vertx vertx, final TestContext context, final boolean doesExist) {
        Async async = context.async();
        vertx.fileSystem().exists(REPORT_FILE_PATH, res -> {
            Assert.assertEquals(doesExist, res.result());
            async.complete();
        });
        async.awaitSuccess();
    }

    public static void deleteReportFile(final Vertx vertx, final TestContext context) {
        Async async = context.async();
        vertx.fileSystem().exists(REPORT_FILE_PATH, res -> {
            if (res.succeeded()) {
                vertx.fileSystem().delete(REPORT_FILE_PATH, v -> async.complete());
            }
        });
        async.awaitSuccess();
    }
}
