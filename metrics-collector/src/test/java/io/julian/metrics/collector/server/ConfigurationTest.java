package io.julian.metrics.collector.server;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {
    @Test
    public void TestInit() {
        Configuration configuration = new Configuration();
        Assert.assertEquals(Configuration.DEFAULT_OPENAPI_SPEC_LOCATION, configuration.getOpenApiSpecLocation());
        Assert.assertEquals(Configuration.DEFAULT_REPORT_FILE_PATH, configuration.getReportPath());
    }
}
