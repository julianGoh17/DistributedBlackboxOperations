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
    private final AtomicInteger succeededPuts = new AtomicInteger();
    private final AtomicInteger failedPuts = new AtomicInteger();

    public void incrementSuccessMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                succeededGets.getAndIncrement();
                break;
            case POST:
                succeededPosts.getAndIncrement();
                break;
            case PUT:
                succeededPuts.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public void incrementFailedMethod(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                failedGets.getAndIncrement();
                break;
            case POST:
                failedPosts.getAndIncrement();
                break;
            case PUT:
                failedPuts.getAndIncrement();
                break;
        }
        log.traceExit();
    }

    public int getSucceeded(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                return log.traceExit(succeededGets.get());
            case POST:
                return log.traceExit(succeededPosts.get());
            case PUT:
                return log.traceExit(succeededPuts.get());
        }
        return log.traceExit(0);
    }

    public int getSucceeded() {
        log.traceEntry();
        return log.traceExit(getSucceeded(RequestMethod.GET) + getSucceeded(RequestMethod.POST) + getSucceeded(RequestMethod.PUT));
    }

    public int getFailed(final RequestMethod method) {
        log.traceEntry(() -> method);
        switch (method) {
            case GET:
                return log.traceExit(failedGets.get());
            case POST:
                return log.traceExit(failedPosts.get());
            case PUT:
                return log.traceExit(failedPuts.get());
        }
        return log.traceExit(0);
    }

    public int getFailed() {
        log.traceEntry();
        return log.traceExit(getFailed(RequestMethod.GET) + getFailed(RequestMethod.POST) + getFailed(RequestMethod.PUT));
    }

    public int getTotal() {
        log.traceEntry();
        return log.traceExit(getSucceeded() + getFailed());
    }
}
