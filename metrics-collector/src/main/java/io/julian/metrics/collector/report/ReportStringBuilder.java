package io.julian.metrics.collector.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReportStringBuilder {
    private final static Logger log = LogManager.getLogger(ReportStringBuilder.class);
    private final StringBuilder builder = new StringBuilder();

    public String toString() {
        log.traceEntry();
        return log.traceExit(builder.toString());
    }

    public void appendLine(final String line) {
        log.traceEntry(() -> line);
        builder.append(line);
        builder.append("\n");
        log.traceExit();
    }
}
