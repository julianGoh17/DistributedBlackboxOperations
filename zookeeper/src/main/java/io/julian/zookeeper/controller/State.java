package io.julian.zookeeper.controller;

import io.julian.zookeeper.models.Proposal;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class State {
    private static final Logger log = LogManager.getLogger(State.class.getName());
    private final ArrayList<Proposal> history = new ArrayList<>();
    private final AtomicInteger lastAcceptedIndex = new AtomicInteger();
    private final Vertx vertx;

    public State(final Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<Void> addProposal(final Proposal proposal) {
        log.traceEntry(() -> proposal);
        log.info("Adding proposal to history");
        Promise<Void> write = Promise.promise();
        vertx.executeBlocking(future -> {
            try {
                history.add(proposal);
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        },
            res -> {
                if (res.succeeded()) {
                    log.info("Completed adding proposal to history");
                    write.complete();
                } else {
                    log.info("Could not add proposal to history");
                    log.error(res.cause());
                    write.fail(res.cause());
                }
            });
        return log.traceExit(write.future());
    }

    public boolean doesExistOutstandingTransaction(final float nextCounter) {
        log.traceEntry(() -> nextCounter);
        log.info(String.format("Checking for outstanding transaction with smaller counter than '%f'", nextCounter));
        for (int i = lastAcceptedIndex.get(); i < history.size(); i++) {
            if (history.get(i).getTransactionId().getCounter() < nextCounter) {
                log.info(String.format("Exists outstanding transaction with counter '%f'", history.get(i).getTransactionId().getCounter()));
                return log.traceExit(true);
            }
        }
        log.info(String.format("No outstanding transaction with counter smaller than '%f'", nextCounter));
        return log.traceExit(false);
    }

    public void setLastAcceptedIndex(final int epoch) {
        log.traceEntry(() -> epoch);
        if (lastAcceptedIndex.get() < epoch) {
            log.info(String.format("Setting last accepted index to '%d'", epoch));
            lastAcceptedIndex.set(epoch);
        } else {
            log.info(String.format("Skipping setting last accepted index to '%d' as current index '%s' is higher",
                epoch, lastAcceptedIndex.get()));
        }
        log.traceExit();
    }

    public ArrayList<Proposal> getHistory() {
        log.traceEntry();
        return log.traceExit(this.history);
    }

    public int getLastAcceptedIndex() {
        log.traceEntry();
        return log.traceExit(lastAcceptedIndex.get());
    }
}
