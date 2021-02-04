package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;

// Require this class or else we can't perform jsonObject.mapTo() to create the LocalDateTime object
public class CoordinationTimestamp {
    private static final Logger log = LogManager.getLogger(CoordinationTimestamp.class.getName());

    private final LocalDateTime time;

    public CoordinationTimestamp(final LocalDateTime time) {
        this.time = time;
    }

    public CoordinationTimestamp() {
        this.time = LocalDateTime.now();
    }

    @JsonCreator
    public static CoordinationTimestamp fromJson(final String timestamp) {
        log.traceEntry(() -> timestamp);
        return log.traceExit(new CoordinationTimestamp(LocalDateTime.parse(timestamp)));
    }

    @JsonValue
    public String toValue() {
        log.traceEntry();
        return log.traceExit(this.time.toString());
    }

    public LocalDateTime toLocalDateTime() {
        return time;
    }
}
