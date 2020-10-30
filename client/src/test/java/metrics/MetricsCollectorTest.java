package metrics;

import io.julian.client.metrics.MetricsCollector;
import io.julian.client.model.operation.Operation;
import org.junit.Assert;
import org.junit.Test;

public class MetricsCollectorTest {
    @Test
    public void TestMetricsCollectorInitializesWithZero() {
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getTotalMessages());
        Assert.assertEquals(0, collector.getSucceeded());
        Assert.assertEquals(0, collector.getFailed());
    }

    @Test
    public void TestMetricsCollectorIncrementsCorrectly() {
        MetricsCollector collector = new MetricsCollector();
        Assert.assertEquals(0, collector.getTotalMessages());
        Assert.assertEquals(0, collector.getSucceeded());
        Assert.assertEquals(0, collector.getFailed());

        collector.addMetric(new Operation(), true);
        Assert.assertEquals(1, collector.getTotalMessages());
        Assert.assertEquals(1, collector.getSucceeded());
        Assert.assertEquals(0, collector.getFailed());

        collector.addMetric(new Operation(), false);
        Assert.assertEquals(2, collector.getTotalMessages());
        Assert.assertEquals(1, collector.getSucceeded());
        Assert.assertEquals(1, collector.getFailed());
    }
}
