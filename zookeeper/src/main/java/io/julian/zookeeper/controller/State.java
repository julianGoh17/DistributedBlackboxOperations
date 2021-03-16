package io.julian.zookeeper.controller;

import io.julian.server.api.exceptions.NoIDException;
import io.julian.server.api.exceptions.SameIDException;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class State {
    private static final Logger log = LogManager.getLogger(State.class.getName());
    public static final int MAX_RETRIES = 5;

    public static final String HISTORY_KEY = "history";
    public static final String LEADER_EPOCH_KEY = "leader_epoch";
    public static final String COUNTER_KEY = "counter";
    public static final String LAST_ACCEPTED_INDEX_KEY = "last_accepted_index";

    private List<Proposal> history;
    private final AtomicInteger lastAcceptedIndex;
    private final Vertx vertx;
    private final MessageStore messageStore;
    private final AtomicInteger leaderEpoch;
    private final AtomicInteger counter;

    public State(final Vertx vertx, final MessageStore messageStore) {
        this.history = new ArrayList<>();
        this.lastAcceptedIndex = new AtomicInteger();
        this.vertx = vertx;
        this.messageStore = messageStore;
        this.leaderEpoch = new AtomicInteger();
        this.counter = new AtomicInteger();
    }

    // Only used for JSON conversion
    public State(final List<Proposal> history, final int lastAcceptedIndex, final int leaderEpoch, final int counter) {
        this.history = history;
        this.lastAcceptedIndex = new AtomicInteger(lastAcceptedIndex);
        this.vertx = null;
        this.messageStore = null;
        this.leaderEpoch = new AtomicInteger(leaderEpoch);
        this.counter = new AtomicInteger(counter);
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

    public Future<Void> processStateUpdate(final Zxid id) {
        log.traceEntry(() -> id);
        Promise<Void> update = Promise.promise();
        AtomicInteger retries = new AtomicInteger();
        this.vertx.setPeriodic(500, timerID -> {
            if (doesExistOutstandingTransaction(id.getCounter())) {
                if (retries.get() < MAX_RETRIES) {
                    log.info(String.format("Loop '%d' retried '%d' time(s) for outstanding transactions to complete", timerID, retries.get() + 1));
                    retries.incrementAndGet();
                } else {
                    Exception exception = new Exception(String.format("State update timeout for '%s'", id.toString()));
                    log.error(exception);
                    update.fail(exception);
                    vertx.cancelTimer(timerID);
                }
            } else {
                try {
                    int index = retrieveStateUpdateIndex(id);
                    ClientMessage message = retrieveStateUpdate(index);
                    if (message == null) {
                        update.fail(String.format("State update with %s does not exist", id.toString()));
                        vertx.cancelTimer(timerID);
                    }
                    if (HTTPRequest.POST.equals(message.getRequest())) {
                        log.info("Processing POST state update");
                        messageStore.addMessageToServer(message.getMessageId(), message.getMessage());
                    } else {
                        log.info("Processing DELETE state update");
                        messageStore.deleteMessageFromServer(message.getMessageId());
                    }
                    setCounterToLatest(id.getCounter());
                    setLastAcceptedIndexIfGreater(index);
                    update.complete();
                } catch (SameIDException | NoIDException e) {
                    log.info(String.format("Couldn't process state update %s", id.toString()));
                    log.error(e);
                    update.fail(e.getMessage());
                }
                vertx.cancelTimer(timerID);
            }
        });
        return log.traceExit(update.future());
    }

    public int retrieveStateUpdateIndex(final Zxid id) {
        log.traceEntry(() -> id);
        log.info(String.format("Retrieving state update with %s id", id.toString()));
        for (int i = lastAcceptedIndex.get(); i < history.size(); i++) {
            if (history.get(i).getTransactionId().equals(id)) {
                log.info(String.format("Successfully retrieved state update with %s id", id.toString()));
                return log.traceExit(i);
            }
        }
        log.info(String.format("Could not find state update with %s id", id.toString()));
        return log.traceExit(-1);
    }

    public ClientMessage retrieveStateUpdate(final int i) {
        log.traceEntry(() -> i);
        return log.traceExit(history.get(i).getNewState());
    }

    public boolean doesExistOutstandingTransaction(final int nextCounter) {
        log.traceEntry(() -> nextCounter);
        log.info(String.format("Checking for outstanding transaction with smaller counter than '%d'", nextCounter));
        for (int i = lastAcceptedIndex.get(); i < history.size(); i++) {
            if (history.get(i).getTransactionId().getCounter() < nextCounter) {
                log.info(String.format("Exists outstanding transaction with counter '%d'", history.get(i).getTransactionId().getCounter()));
                return log.traceExit(true);
            }
        }
        log.info(String.format("No outstanding transaction with counter smaller than '%d'", nextCounter));
        return log.traceExit(false);
    }

    public void setCounterToLatest(final int counter) {
        log.traceEntry(() -> counter);
        if (this.counter.get() < counter) {
            log.info(String.format("Updating counter to %d", counter));
            this.counter.set(counter);
        }
        log.traceExit();
    }

    public void setLastAcceptedIndexIfGreater(final int index) {
        log.traceEntry(() -> index);
        final int nextIndex = index + 1;
        if (lastAcceptedIndex.get() < nextIndex) {
            log.info(String.format("Setting last accepted index to '%d'", nextIndex));
            lastAcceptedIndex.set(nextIndex);
        } else {
            log.info(String.format("Skipping setting last accepted index to '%d' as current index '%s' is higher",
                nextIndex, lastAcceptedIndex.get()));
        }
        log.traceExit();
    }

    public void setLastAcceptedIndex(final int index) {
        log.traceEntry(() -> index);
        lastAcceptedIndex.set(index);
        log.traceExit();
    }

    public List<Proposal> getHistory() {
        log.traceEntry();
        return log.traceExit(this.history);
    }

    public MessageStore getMessageStore() {
        log.traceEntry();
        return log.traceExit(this.messageStore);
    }

    public int getLastAcceptedIndex() {
        log.traceEntry();
        return log.traceExit(lastAcceptedIndex.get());
    }

    public int getLeaderEpoch() {
        log.traceEntry();
        return log.traceExit(leaderEpoch.get());
    }

    public int getCounter() {
        log.traceEntry();
        return log.traceExit(counter.get());
    }

    public int incrementAndGetCounter() {
        log.traceEntry();
        return log.traceExit(counter.incrementAndGet());
    }

    public void setLeaderEpoch(final int epoch) {
        log.traceEntry(() -> epoch);
        leaderEpoch.set(epoch);
        log.traceExit();
    }

    public void setCounter(final int counter) {
        log.traceEntry(() -> counter);
        this.counter.set(counter);
        log.traceExit();
    }

    public boolean isLaterThanState(final State other) {
        log.traceEntry(() -> other);
        if (leaderEpoch.get() == other.getLeaderEpoch()) {
            log.info(String.format("State updated to latest epoch at (%d,%d)", other.getLeaderEpoch(), other.getCounter()));
            return log.traceExit(counter.get() >= other.getCounter());
        }
        return log.traceExit(leaderEpoch.get() > other.getLeaderEpoch());
    }

    public void setState(final State other) {
        log.traceEntry(() -> other);
        setCounter(other.getCounter());
        setLeaderEpoch(other.getLeaderEpoch());
        this.history = other.getHistory();
        this.lastAcceptedIndex.set(other.getLastAcceptedIndex());
        log.info(String.format("State updated to latest epoch at (%d,%d)", getLeaderEpoch(), getCounter()));
        log.traceExit();
    }

    public JsonObject toJson() {
        log.traceEntry();
        final JsonArray historyJson = new JsonArray();
        this.history
            .forEach(proposal -> historyJson.add(proposal.toJson()));

        return log.traceExit(new JsonObject()
            .put(HISTORY_KEY, historyJson)
            .put(LEADER_EPOCH_KEY, leaderEpoch.get())
            .put(COUNTER_KEY, counter.get())
            .put(LAST_ACCEPTED_INDEX_KEY, lastAcceptedIndex.get()));
    }

    public static State fromJson(final JsonObject json) {
        log.traceEntry(() -> json);
        final ArrayList<Proposal> history = new ArrayList<>();
        json.getJsonArray(HISTORY_KEY)
            .stream()
            .forEach(proposal -> history.add(Proposal.mapFrom((JsonObject) proposal)));

        return log.traceExit(new State(
            history,
            json.getInteger(LAST_ACCEPTED_INDEX_KEY),
            json.getInteger(LEADER_EPOCH_KEY),
            json.getInteger(COUNTER_KEY)
            ));
    }
}
