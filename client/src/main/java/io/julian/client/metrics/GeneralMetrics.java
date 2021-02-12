package io.julian.client.metrics;

import io.julian.client.model.RequestMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class GeneralMetrics {
    private static final Logger log = LogManager.getLogger(GeneralMetrics.class.getName());
    private final AtomicInteger succeededGets = new AtomicInteger();
    private final AtomicInteger failedGets = new AtomicInteger();
    private final AtomicInteger succeededPosts = new AtomicInteger();
    private final AtomicInteger failedPosts = new AtomicInteger();
    private final AtomicInteger succeededDeletes = new AtomicInteger();
    private final AtomicInteger failedDeletes = new AtomicInteger();

    public void incrementSuccessMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        log.info(String.format("Incrementing success for '%s'", method));
        switch (method) {
            case GET:
                succeededGets.getAndIncrement();
                break;
            case POST:
                succeededPosts.getAndIncrement();
                break;
            case DELETE:
                succeededDeletes.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public void incrementFailedMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        log.info(String.format("Incrementing fail for '%s'", method));
        switch (method) {
            case GET:
                failedGets.getAndIncrement();
                break;
            case POST:
                failedPosts.getAndIncrement();
                break;
            case DELETE:
                failedDeletes.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public int getSucceeded(final RequestMethod method) {
        log.traceEntry(() -> method);
        log.info(String.format("Retrieving successes for '%s'", method));
        switch (method) {
            case GET:
                return log.traceExit(succeededGets.get());
            case POST:
                return log.traceExit(succeededPosts.get());
            case DELETE:
                return log.traceExit(succeededDeletes.get());
        }
        return log.traceExit(0);
    }

    public int getSucceeded() {
        log.traceEntry();
        return log.traceExit(getSucceeded(RequestMethod.GET) + getSucceeded(RequestMethod.POST) + getSucceeded(RequestMethod.DELETE));
    }

    public int getFailed(final RequestMethod method) {
        log.traceEntry(() -> method);
        log.info(String.format("Retrieving fails for '%s'", method));
        switch (method) {
            case GET:
                return log.traceExit(failedGets.get());
            case POST:
                return log.traceExit(failedPosts.get());
            case DELETE:
                return log.traceExit(failedDeletes.get());
        }
        return log.traceExit(0);
    }

    public int getFailed() {
        log.traceEntry();
        return log.traceExit(getFailed(RequestMethod.GET) + getFailed(RequestMethod.POST) + getFailed(RequestMethod.DELETE));
    }

    public int getTotal() {
        log.traceEntry();
        return log.traceExit(getSucceeded() + getFailed());
    }
}
