package io.julian.metrics.collector.report;

import org.junit.Assert;
import org.junit.Test;

public class ReportStringBuilderTest {
    @Test
    public void TestAppendLine() {
        ReportStringBuilder builder = new ReportStringBuilder();
        String line = "random line 1235Q@";
        builder.appendLine(line);
        Assert.assertEquals(line + "\n", builder.toString());
    }

    @Test
    public void TestAppend() {
        ReportStringBuilder builder = new ReportStringBuilder();
        String line = "random line 1235Q@";
        builder.append(line);
        Assert.assertEquals(line, builder.toString());
    }
}
