package metrics;

import io.julian.client.metrics.GeneralMetrics;
import io.julian.client.model.RequestMethod;
import org.junit.Assert;
import org.junit.Test;

public class GeneralMetricsTest {
    @Test
    public void TestGeneralMetricsInitializesCorrectly() {
        GeneralMetrics generalMetrics = new GeneralMetrics();
        Assert.assertEquals(0, generalMetrics.getTotal());
        Assert.assertEquals(0, generalMetrics.getSucceeded());
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, generalMetrics.getFailed());
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.POST));
    }
    
    @Test 
    public void TestGeneralMetricsIncrementsSuccessCorrectly() {
        GeneralMetrics generalMetrics = new GeneralMetrics();

        generalMetrics.incrementSuccessMethod(RequestMethod.GET);
        Assert.assertEquals(1, generalMetrics.getTotal());
        Assert.assertEquals(1, generalMetrics.getSucceeded());
        Assert.assertEquals(1, generalMetrics.getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, generalMetrics.getFailed());
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.POST));

        generalMetrics.incrementSuccessMethod(RequestMethod.POST);
        Assert.assertEquals(2, generalMetrics.getTotal());
        Assert.assertEquals(2, generalMetrics.getSucceeded());
        Assert.assertEquals(1, generalMetrics.getSucceeded(RequestMethod.GET));
        Assert.assertEquals(1, generalMetrics.getSucceeded(RequestMethod.POST));
        Assert.assertEquals(0, generalMetrics.getFailed());
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.POST));
    }

    @Test
    public void TestGeneralMetricsIncrementsFailCorrectly() {
        GeneralMetrics generalMetrics = new GeneralMetrics();

        generalMetrics.incrementFailedMethod(RequestMethod.GET);
        Assert.assertEquals(1, generalMetrics.getTotal());
        Assert.assertEquals(0, generalMetrics.getSucceeded());
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.POST));
        Assert.assertEquals(1, generalMetrics.getFailed());
        Assert.assertEquals(1, generalMetrics.getFailed(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getFailed(RequestMethod.POST));

        generalMetrics.incrementFailedMethod(RequestMethod.POST);
        Assert.assertEquals(2, generalMetrics.getTotal());
        Assert.assertEquals(0, generalMetrics.getSucceeded());
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.GET));
        Assert.assertEquals(0, generalMetrics.getSucceeded(RequestMethod.POST));
        Assert.assertEquals(2, generalMetrics.getFailed());
        Assert.assertEquals(1, generalMetrics.getFailed(RequestMethod.GET));
        Assert.assertEquals(1, generalMetrics.getFailed(RequestMethod.POST));

    }
}
