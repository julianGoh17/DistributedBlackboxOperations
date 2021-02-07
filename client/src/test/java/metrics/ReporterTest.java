package metrics;

import io.julian.client.exception.ClientException;
import io.julian.client.metrics.GeneralMetrics;
import io.julian.client.metrics.Reporter;
import io.julian.client.model.response.MismatchedResponse;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Action;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.operation.Operation;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.util.Collections;

import static io.julian.client.metrics.Reporter.REPORT_FILE_NAME;

@RunWith(VertxUnitRunner.class)
public class ReporterTest {
    private Vertx vertx;

    // Putting in /tmp so that we have permissions to create file in Github Action pipeline
    private static final String TEST_REPORT_FILE_PATH = "/tmp";
    private final static RequestMethod METHOD = RequestMethod.GET;
    private final static int MESSAGE_NUMBER = 91;
    private final static int EXPECTED_STATUS_CODE = 0;
    private final static int ACTUAL_STATUS_CODE = 40441;
    private final static String ERROR = "Amazing error string";

    private final static String GENERAL_REPORT = "General Statistics:\n" +
        "-------------------\n" +
        "Total Successful Requests: 0\n" +
        "Total Failed Requests: 0\n";
    private final static String GET_REPORT = String.format("General Statistics For %s:\n", RequestMethod.GET.toString()) +
        "---------------------------\n" +
        "Successful Requests: 0\n" +
            "Failed Requests: 0\n";
    private final static String POST_REPORT = String.format("General Statistics For %s:\n", RequestMethod.POST.toString()) +
        "----------------------------\n" +
        "Successful Requests: 0\n" +
        "Failed Requests: 0\n";
    private final static String DELETE_REPORT = String.format("General Statistics For %s:\n", RequestMethod.DELETE.toString()) +
        "------------------------------\n" +
        "Successful Requests: 0\n" +
        "Failed Requests: 0\n";
    private final static String MISMATCHED_RESPONSE_REPORT = String.format("Mismatched Response For %s Request\n", METHOD.toString()) +
        "-----------------------------------\n" +
        String.format("Message Number: %d\n", MESSAGE_NUMBER) +
        String.format("Expected Status Code: %d\n", EXPECTED_STATUS_CODE) +
        String.format("Actual Status Code: %d\n", ACTUAL_STATUS_CODE) +
        String.format("Error: %s\n", ERROR);

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void TestReporterCanCheckFolderPathExists() {
        Reporter reporter = new Reporter();
        try {
            reporter.checkReportFolderExists(TEST_REPORT_FILE_PATH);
        } catch (FileNotFoundException e) {
            Assert.fail();
        }
    }

    @Test
    public void TestReporterCanCheckFolderPathDoesNotExists() {
        Reporter reporter = new Reporter();
        String wrongFilePath = String.format("%s/random-location1234", TEST_REPORT_FILE_PATH);
        try {
            reporter.checkReportFolderExists(wrongFilePath);
            Assert.fail();
        } catch (FileNotFoundException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(String.format("Could not find folder at '%s'", wrongFilePath), e.getMessage());
        }
    }

    @Test
    public void TestReporterCanTellIfNotDirectory(final TestContext context) {
        Reporter reporter = new Reporter();
        String filePath = String.format("%s/random-location1234", TEST_REPORT_FILE_PATH);
        Promise<String> testComplete = Promise.promise();
        vertx.fileSystem().createFile(filePath, context.asyncAssertSuccess(res -> {
            try {
                reporter.checkReportFolderExists(filePath);
                testComplete.fail("Failed to check that folder is file");
            } catch (FileNotFoundException e) {
                testComplete.complete(e.getMessage());
            }
        }));

        testComplete.future().onComplete(context.asyncAssertSuccess(test -> vertx.fileSystem().delete(filePath, context.asyncAssertSuccess(delete -> {
            System.out.println("Successfully deleted file at " + filePath);
            Assert.assertEquals(String.format("Not a folder found at '%s'", filePath), test);
        }))));
    }

    @Test
    public void TestCreateReportFailsWhenWrongFileLocation(final TestContext context) {
        Reporter reporter = new Reporter();
        String wrongFilePath = String.format("%s/random-location1234", TEST_REPORT_FILE_PATH);
        Future<Void> hasWrote = reporter.createReportFile(Collections.singletonList(createTestMismatchedResponse()), new GeneralMetrics(), wrongFilePath, vertx);

        hasWrote.onComplete(context.asyncAssertFailure(throwable ->
            Assert.assertEquals(String.format("java.nio.file.NoSuchFileException: %s/%s", wrongFilePath, REPORT_FILE_NAME), throwable.getMessage())));
    }

    @Test
    public void TestCreateReportSuccessful(final TestContext context) {
        String reportFilePath = String.format("%s/%s", TEST_REPORT_FILE_PATH, REPORT_FILE_NAME);
        Reporter reporter = new Reporter();
        Future<Void> hasWrote = reporter.createReportFile(Collections.singletonList(createTestMismatchedResponse()), new GeneralMetrics(), TEST_REPORT_FILE_PATH, vertx);

        hasWrote.onComplete(context.asyncAssertSuccess(res -> vertx.fileSystem().exists(reportFilePath, open -> {
            Assert.assertTrue(open.succeeded());
            Assert.assertNull(open.cause());
            vertx.fileSystem().delete(reportFilePath, delete -> {
                if (delete.succeeded()) {
                    System.out.printf("Deleted file '%s'", reportFilePath);
                } else {
                    System.out.printf("Failed to delete file '%s'", reportFilePath);
                }
            });
        })));
    }

    @Test
    public void TestGetReport() {
        Reporter reporter = new Reporter();
        StringBuilder builder = reporter.getReport(Collections.singletonList(createTestMismatchedResponse()), new GeneralMetrics());
        String expected = String.join("\n", GENERAL_REPORT, GET_REPORT, POST_REPORT, DELETE_REPORT, MISMATCHED_RESPONSE_REPORT);
        Assert.assertEquals(expected, builder.toString());
    }

    @Test
    public void TestReporterFormatsMethodGeneralStatisticsForMethodCorrectly() {
        Reporter reporter = new Reporter();
        GeneralMetrics metrics = new GeneralMetrics();
        StringBuilder builder = new StringBuilder();
        reporter.getGeneralStatistics(metrics, RequestMethod.GET, builder);
        Assert.assertEquals(GET_REPORT, builder.toString());

        builder = new StringBuilder();
        reporter.getGeneralStatistics(metrics, RequestMethod.POST, builder);
        Assert.assertEquals(POST_REPORT, builder.toString());

        builder = new StringBuilder();
        reporter.getGeneralStatistics(metrics, RequestMethod.DELETE, builder);
        Assert.assertEquals(DELETE_REPORT, builder.toString());
    }

    @Test
    public void TestReporterFormatsMethodGeneralStatisticsCorrectly() {
        Reporter reporter = new Reporter();
        GeneralMetrics metrics = new GeneralMetrics();
        StringBuilder builder = new StringBuilder();
        reporter.getGeneralStatistics(metrics, builder);
        Assert.assertEquals(GENERAL_REPORT, builder.toString());
    }

    @Test
    public void TestReporterFormatsMismatchedResponseEntry() {
        Reporter reporter = new Reporter();
        StringBuilder builder = new StringBuilder();
        reporter.createMismatchedResponseEntry(createTestMismatchedResponse(), builder);

        Assert.assertEquals(MISMATCHED_RESPONSE_REPORT,
            builder.toString());
    }

    @Test
    public void TestReporterFormatsSeparatorCorrectly() {
        Reporter reporter = new Reporter();
        String randomHeader = "random-header-1235134@";
        Assert.assertEquals(randomHeader.length(), reporter.getHeaderSeparator(randomHeader).length());
    }

    private MismatchedResponse createTestMismatchedResponse() {
        Operation operation = new Operation();
        Action action = new Action();
        action.setMethod(METHOD);
        action.setMessageNumber(MESSAGE_NUMBER);
        operation.setAction(action);

        Expected expected = new Expected(1, null);
        expected.setStatusCode(EXPECTED_STATUS_CODE);
        operation.setExpected(expected);

        return new MismatchedResponse(operation, new ClientException(ERROR, ACTUAL_STATUS_CODE));
    }
}
