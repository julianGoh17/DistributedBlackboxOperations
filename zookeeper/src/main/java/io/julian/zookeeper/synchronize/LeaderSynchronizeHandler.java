package io.julian.zookeeper.synchronize;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.discovery.LeaderDiscoveryHandler;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LeaderSynchronizeHandler {
    private static final Logger log = LogManager.getLogger(LeaderDiscoveryHandler.class);

    private final State state;
    private final RegistryManager manager;
    private final ServerClient client;
    private final AtomicInteger acknowledgements = new AtomicInteger();
    private final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages;

    public LeaderSynchronizeHandler(final State state, final RegistryManager manager, final ServerClient client, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        this.state = state;
        this.manager = manager;
        this.client = client;
        this.deadCoordinationMessages = deadCoordinationMessages;
    }

    public Future<Void> broadcastState() {
        log.traceEntry();
        log.info("Leader broadcasting state to followers");
        Promise<Void> res = Promise.promise();
        List<Future> broadcast = manager
            .getOtherServers()
            .stream()
            .map(server -> client.sendCoordinateMessageToServer(server, getCoordinationMessage()))
            .collect(Collectors.toList());

        CompositeFuture.all(broadcast)
            .onSuccess(v -> {
                log.info("Leader successfully broadcast state to followers");
                acknowledgements.set(0);
                res.complete();
            })
            .onFailure(cause -> {
                log.info("Leader unsuccessfully broadcast state to followers");
                deadCoordinationMessages.add(getCoordinationMessage());
                log.error(cause);
                res.fail(cause);
            });

        return log.traceExit(res.future());
    }

    public void incrementAcknowledgement() {
        log.traceEntry();
        log.info("Leader has received synchronize acknowledgement from follower");
        acknowledgements.getAndIncrement();
        log.traceExit();
    }

    public boolean hasReceivedAcknowledgementsFromFollowers() {
        log.traceEntry();
        return log.traceExit(acknowledgements.get() == manager.getOtherServers().size());
    }

    public CoordinationMessage getCoordinationMessage() {
        log.traceEntry();
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, null, SynchronizeHandler.SYNCHRONIZE_TYPE),
            null,
            state.toJson()));
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(state);
    }

    public int getAcknowledgements() {
        log.traceEntry();
        return log.traceExit(acknowledgements.get());
    }

    public ConcurrentLinkedQueue<CoordinationMessage> getDeadCoordinationMessages() {
        log.traceEntry();
        return log.traceExit(deadCoordinationMessages);
    }
}
