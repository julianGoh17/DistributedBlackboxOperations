package metrics;

import io.julian.client.exception.ClientException;
import io.julian.client.metrics.MetricsCollector;
import io.julian.client.model.RequestMethod;
import io.julian.client.model.operation.Action;
import io.julian.client.model.operation.Expected;
import io.julian.client.model.operation.Operation;
import io.julian.client.model.operation.OverviewComparison;
import io.julian.server.models.response.ServerOverview;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class MetricsCollectorTest {
    private final Operation getOperation = createOperation(RequestMethod.GET);
    private final Operation postOperation = createOperation(RequestMethod.POST);
    private final Operation deleteOperation = createOperation(RequestMethod.DELETE);

    private final static int MESSAGE_NUMBER = 1;
    private final static int EXPECTED_MESSAGE_NUMBER = 2;
    private final static int EXPECTED_STATUS_CODE = 200;
    private final static int ACTUAL_STATUS_CODE = 404;

    private final static String EXCEPTION_MESSAGE = "Random error";

    @Test
    public void TestMetricsCollectorInitializesWithZero() {
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getFailed());
    }

    @Test
    public void TestMetricsCollectorIncrementsSuccessesCorrectly() {
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getGeneral().getFailed());
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getMismatchedResponses().size());

        collector.addSucceededMetric(getOperation);
        Assert.assertEquals(1, collector.getGeneral().getTotal());
        Assert.assertEquals(1, collector.getGeneral().getSucceeded());
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getGeneral().getFailed());
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getMismatchedResponses().size());

        collector.addSucceededMetric(postOperation);
        Assert.assertEquals(2, collector.getGeneral().getTotal());
        Assert.assertEquals(2, collector.getGeneral().getSucceeded());
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getGeneral().getFailed());
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getMismatchedResponses().size());

        collector.addSucceededMetric(deleteOperation);
        Assert.assertEquals(3, collector.getGeneral().getTotal());
        Assert.assertEquals(3, collector.getGeneral().getSucceeded());
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(1, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getGeneral().getFailed());
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getMismatchedResponses().size());
    }

    @Test
    public void TestMetricsCollectorIncrementsFailedCorrectly() {
        ClientException exception = new ClientException(EXCEPTION_MESSAGE, ACTUAL_STATUS_CODE);
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getGeneral().getFailed());
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(0, collector.getMismatchedResponses().size());

        collector.addFailedMetric(getOperation, exception);
        Assert.assertEquals(1, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(1, collector.getGeneral().getFailed());
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(1, collector.getMismatchedResponses().size());

        collector.addFailedMetric(postOperation, exception);
        Assert.assertEquals(2, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(2, collector.getGeneral().getFailed());
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(2, collector.getMismatchedResponses().size());

        collector.addFailedMetric(deleteOperation, exception);
        Assert.assertEquals(3, collector.getGeneral().getTotal());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded());
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, collector.getGeneral().getSucceeded(RequestMethod.DELETE));
        Assert.assertEquals(3, collector.getGeneral().getFailed());
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.GET));
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.POST));
        Assert.assertEquals(1, collector.getGeneral().getFailed(RequestMethod.DELETE));
        Assert.assertEquals(3, collector.getMismatchedResponses().size());
    }

    @Test
    public void TestMetricsCollectorAddsFailedMetricCorrectly() {
        ClientException exception = new ClientException(EXCEPTION_MESSAGE, ACTUAL_STATUS_CODE);
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getMismatchedResponses().size());

        collector.addFailedMetric(getOperation, exception);
        Assert.assertEquals(1, collector.getMismatchedResponses().size());
        Assert.assertEquals(RequestMethod.GET, collector.getMismatchedResponses().get(0).getMethod());
        Assert.assertEquals(ACTUAL_STATUS_CODE, collector.getMismatchedResponses().get(0).getActualStatusCode());
        Assert.assertEquals(EXPECTED_STATUS_CODE, collector.getMismatchedResponses().get(0).getExpectedStatusCode());
        Assert.assertEquals(MESSAGE_NUMBER, collector.getMismatchedResponses().get(0).getMessageNumber());
        Assert.assertEquals(EXCEPTION_MESSAGE, collector.getMismatchedResponses().get(0).getError());

        collector.addFailedMetric(postOperation, exception);
        Assert.assertEquals(2, collector.getMismatchedResponses().size());
        Assert.assertEquals(RequestMethod.POST, collector.getMismatchedResponses().get(1).getMethod());
        Assert.assertEquals(ACTUAL_STATUS_CODE, collector.getMismatchedResponses().get(1).getActualStatusCode());
        Assert.assertEquals(EXPECTED_STATUS_CODE, collector.getMismatchedResponses().get(1).getExpectedStatusCode());
        Assert.assertEquals(MESSAGE_NUMBER, collector.getMismatchedResponses().get(1).getMessageNumber());
        Assert.assertEquals(EXCEPTION_MESSAGE, collector.getMismatchedResponses().get(1).getError());

        collector.addFailedMetric(deleteOperation, exception);
        Assert.assertEquals(3, collector.getMismatchedResponses().size());
        Assert.assertEquals(RequestMethod.DELETE, collector.getMismatchedResponses().get(2).getMethod());
        Assert.assertEquals(ACTUAL_STATUS_CODE, collector.getMismatchedResponses().get(2).getActualStatusCode());
        Assert.assertEquals(EXPECTED_STATUS_CODE, collector.getMismatchedResponses().get(2).getExpectedStatusCode());
        Assert.assertEquals(MESSAGE_NUMBER, collector.getMismatchedResponses().get(2).getMessageNumber());
        Assert.assertEquals(EXCEPTION_MESSAGE, collector.getMismatchedResponses().get(2).getError());
    }

    @Test
    public void TestMetricsCollectorAddsOverviewComparison() {
        MetricsCollector collector = new MetricsCollector();
        collector.addComparisonCheck(Collections.emptyList(), new ServerOverview("local", 999, 0, Collections.emptyList()));
        Assert.assertEquals(1, collector.getOverviewComparisons().size());
    }

    private Operation createOperation(final RequestMethod method) {
        Operation operation = new Operation();
        Action action = new Action();
        action.setMethod(method);
        action.setMessageNumber(MESSAGE_NUMBER);
        operation.setAction(action);

        Expected expected = new Expected(1);
        expected.setStatusCode(EXPECTED_STATUS_CODE);
        operation.setExpected(expected);

        return operation;
    }
}
