package io.julian.metrics.collector.report.filters;

import io.julian.metrics.collector.models.TrackedMessage;
import io.julian.metrics.collector.tracking.MessageStatus;
import org.junit.Assert;
import org.junit.Test;

public class GenericStatisticsTest {
    public final static String MESSAGE_ID = "id";

    @Test
    public void TestGeneralStatistics() {
        MessageStatus status = new MessageStatus();
        status.addTrackedMessage(new TrackedMessage(200, MESSAGE_ID, 40.431f));

        String expectedEntry = String.format("%s: 40.43 bytes\n", GenericStatistics.AVERAGE_MESSAGE_SIZE_KEY) +
            String.format("%s: 1\n", GenericStatistics.TOTAL_MESSAGES_KEY) +
            String.format("%s: 0\n", GenericStatistics.TOTAL_FAILED_MESSAGES_KEY) +
            String.format("%s: 1\n", GenericStatistics.TOTAL_SUCCEEDED_MESSAGES_KEY) +
            "\n";

        GenericStatistics statistics = new GenericStatistics();
        Assert.assertEquals(expectedEntry, statistics.toReportEntry(status));
    }
}
