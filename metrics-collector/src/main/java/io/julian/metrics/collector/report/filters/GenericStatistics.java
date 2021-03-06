package io.julian.metrics.collector.report.filters;

import io.julian.metrics.collector.report.ReportStringBuilder;
import io.julian.metrics.collector.tracking.MessageStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GenericStatistics {
    private final static Logger log = LogManager.getLogger(GenericStatistics.class);

    public final static String AVERAGE_MESSAGE_SIZE_KEY = "Average Message Size";
    public final static String TOTAL_MESSAGES_KEY = "Total Messages";
    public final static String TOTAL_FAILED_MESSAGES_KEY = "Total Failed Messages";
    public final static String TOTAL_SUCCEEDED_MESSAGES_KEY = "Total Succeeded Messages";
    public final static String TOTAL_TIME_KEY = "Total Time";

    public String toReportEntry(final MessageStatus messageStatus) {
        log.traceEntry(() -> messageStatus);
        log.info("Creating Generic Report Entry");
        ReportStringBuilder builder = new ReportStringBuilder();

        final int totalMessages = messageStatus.getFailedMessages() + messageStatus.getSuccessfulMessages();
        builder.appendLine(String.format("%s: %.2f bytes", AVERAGE_MESSAGE_SIZE_KEY, messageStatus.getTotalMessageSize() / totalMessages));
        builder.appendLine(String.format("%s: %d", TOTAL_MESSAGES_KEY, totalMessages));
        builder.appendLine(String.format("%s: %d", TOTAL_FAILED_MESSAGES_KEY, messageStatus.getFailedMessages()));
        builder.appendLine(String.format("%s: %d", TOTAL_SUCCEEDED_MESSAGES_KEY, messageStatus.getSuccessfulMessages()));
        builder.appendLine(String.format("%s: %s", TOTAL_TIME_KEY, messageStatus.getTimeDifference()));
        builder.appendLine("");

        return log.traceExit(builder.toString());
    }
}
