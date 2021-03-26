package io.julian;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.discovery.DiscoveryHandler;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.Stage;
import io.julian.zookeeper.synchronize.SynchronizeHandler;
import io.julian.zookeeper.write.WriteHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandler {
    private static final Logger log = LogManager.getLogger(MessageHandler.class);
    private final State state;
    private final CandidateInformationRegistry registry;
    private final Controller controller;
    private final RegistryManager registryManager;
    private final ServerClient client;

    private final LeadershipElectionHandler electionHandler;
    private final DiscoveryHandler discoveryHandler;
    private final SynchronizeHandler synchronizeHandler;
    private final WriteHandler writeHandler;
    private boolean hasNotBroadcast = true;

    public MessageHandler(final Controller controller, final MessageStore messageStore, final Vertx vertx, final RegistryManager manager, final ServerClient client, final ConcurrentLinkedQueue<CoordinationMessage> deadCoordinationMessages) {
        long candidateNumber = generateCandidateNumber(vertx.deploymentIDs().size());
        this.state = new State(vertx, messageStore);
        this.registry = initializeCandidateInformationRegistry(controller.getConfiguration(), candidateNumber);
        this.electionHandler = new LeadershipElectionHandler(candidateNumber, this.registry, client, manager, controller, deadCoordinationMessages);
        this.discoveryHandler = new DiscoveryHandler(controller, state, registry, manager, client, deadCoordinationMessages);
        this.writeHandler = new WriteHandler(controller, state, electionHandler.getCandidateRegistry(), client, manager, deadCoordinationMessages);
        this.synchronizeHandler = new SynchronizeHandler(vertx, state, manager, client, registry, controller, deadCoordinationMessages);
        this.controller = controller;
        this.registryManager = manager;
        this.client = client;
    }

    public Future<Void> actOnCoordinateMessage(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        Stage messageStage = Stage.fromType(message.getMetadata().getType());
        updateFollowerState(message);
        if (messageStage.equals(Stage.WRITE)) {
            log.info("Received state update");
            return log.traceExit(this.writeHandler.handleCoordinationMessage(message));
        } else if (messageStage.equals(Stage.DISCOVERY)) {
            return log.traceExit(handleDiscoveryUpdate(message));
        } else if (messageStage.equals(Stage.SYNCHRONIZE)) {
            log.info("Received Synchronize update");
            return log.traceExit(this.synchronizeHandler.handleCoordinationMessage(message));
        } else if (messageStage.equals(Stage.SETUP)) {
            log.info("Received Election update");
            return log.traceExit(addCandidateInformation(message.getDefinition().mapTo(CandidateInformation.class)));
        } else {
            log.info("Received broadcast candidate number request");
            return log.traceExit(broadcastCandidateNumber());
        }
    }

    public Future<Void> actOnInitialMessage(final ClientMessage message) {
        log.traceEntry(() -> message);
        Promise<Void> res = Promise.promise();
        writeHandler.handleClientMessage(message)
            .onSuccess(res::complete)
            .onFailure(throwable -> {
                log.info("Failed to deliver update to followers");
                log.error(throwable.getMessage());
                res.fail(throwable);
            });
        return log.traceExit(res.future());
    }


    /**
     * Adds candidate information in message received from other server to candidate registry
     * @param information another server's candidate information
     */
    private Future<Void> addCandidateInformation(final CandidateInformation information) {
        log.traceEntry(() -> information);
        electionHandler.addCandidateInformation(information);
        return log.traceExit(broadcastCandidateNumber()
            .compose(v -> updateLeader()));
    }

    /**
     * Will send the servers candidate number to all other servers if it has not broadcast to other servers already
     */
    private Future<Void> broadcastCandidateNumber() {
        log.traceEntry();
        Promise<Void> res = Promise.promise();
        if (hasNotBroadcast) {
            log.info("Broadcasting candidate information to other servers");
            electionHandler.broadcast()
                .onSuccess(v -> res.complete())
                .onFailure(res::fail);
            hasNotBroadcast = false;
            return log.traceExit(res.future());
        }
        return log.traceExit(Future.succeededFuture());
    }

    /**
     * Updates who the server thinks is the leader server if it has received all information from all other servers
     */
    public Future<Void> updateLeader() {
        log.traceEntry();
        if (controller.getLabel().equals("") && electionHandler.canUpdateLeader(registryManager)) {
            log.info("Updating leader of servers");
            electionHandler.updateLeader(registryManager, controller);
            state.setServerStage(Stage.DISCOVERY);
            if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL) && !this.discoveryHandler.hasBroadcastFollowerZXID()) {
                log.info("Starting Discovery phase");
                this.discoveryHandler.reset();
                return log.traceExit(this.discoveryHandler.broadcastToFollowers());
            }
        }
        return log.traceExit(Future.succeededFuture());
    }

    private Future<Void> handleDiscoveryUpdate(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        log.info("Received Discovery update");
        Promise<Void> res = Promise.promise();

        this.discoveryHandler.handleCoordinationMessage(message)
            .onSuccess(v -> {
                if (controller.getLabel().equals(LeadershipElectionHandler.LEADER_LABEL) && this.discoveryHandler.hasEnoughResponses()) {
                    state.setServerStage(Stage.SYNCHRONIZE);
                    log.info("Starting Synchronize Phase");
                    this.synchronizeHandler.broadcastState()
                        .onSuccess(res::complete)
                        .onFailure(res::fail);
                }
            });
        return log.traceExit(res.future());
    }

    public void updateFollowerState(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        if (controller.getLabel().equals(LeadershipElectionHandler.FOLLOWER_LABEL)) {
            if (canChangeStateTo(Stage.SYNCHRONIZE, Stage.WRITE, message.getMetadata().getType())) {
                log.info(String.format("Follower updating to '%s' stage", Stage.WRITE.toString()));
                state.setServerStage(Stage.WRITE);
            } else if (canChangeStateTo(Stage.DISCOVERY, Stage.SYNCHRONIZE, message.getMetadata().getType())) {
                log.info(String.format("Follower updating to '%s' stage", Stage.SYNCHRONIZE.toString()));
                state.setServerStage(Stage.SYNCHRONIZE);
            }
        }
        log.traceExit();
    }

    public boolean canChangeStateTo(final Stage currentStage, final Stage nextStage, final String type) {
        log.traceEntry(() -> currentStage, () -> nextStage, () -> type);
        return log.traceExit(currentStage.equals(state.getServerStage()) && nextStage.equals(Stage.fromType(type)));
    }

    /**
     * Exposed For Testing
     * Generates a random candidate number with many digits which will be used to determine the leadership of a server
     * @return candidate number
     */
    public long generateCandidateNumber(final int offset) {
        log.traceEntry();
        log.info("Generating random candidate number");
        return log.traceExit((long) (Math.random() * Math.pow(10, 10) + offset));
    }

    /**
     * Initializes the candidate registry with the current server's candidate information stored inside
     * @param configuration current server configuration
     * @param candidateNumber current server candidate number
     * @return An initialized candidate registry
     */
    public CandidateInformationRegistry initializeCandidateInformationRegistry(final Configuration configuration, final long candidateNumber) {
        log.traceEntry(() -> configuration, () -> candidateNumber);
        log.info("Initializing candidate registry");
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(configuration.getServerHost(), configuration.getServerPort(), candidateNumber));
        return log.traceExit(registry);
    }

    public State getState() {
        log.traceEntry();
        return log.traceExit(state);
    }

    public Controller getController() {
        log.traceEntry();
        return log.traceExit(controller);
    }

    public CandidateInformationRegistry getRegistry() {
        log.traceEntry();
        return log.traceExit(registry);
    }

    public RegistryManager getRegistryManager() {
        log.traceEntry();
        return log.traceExit(registryManager);
    }

    public ServerClient getClient() {
        log.traceEntry();
        return log.traceExit(client);
    }
}
