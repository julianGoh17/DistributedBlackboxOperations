package io.julian;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.election.BroadcastCandidateInformationHandler;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.write.FollowerWriteHandler;
import io.julian.zookeeper.write.LeaderWriteHandler;
import io.julian.zookeeper.write.WriteHandler;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class ZookeeperAlgorithm extends DistributedAlgorithm {
    private final Logger log = LogManager.getLogger(ZookeeperAlgorithm.class);
    private final CandidateInformationRegistry registry;
    private final LeadershipElectionHandler electionHandler;
    private final WriteHandler writeHandler;
    private boolean hasNotBroadcast = true;

    public ZookeeperAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
        long candidateNumber = generateCandidateNumber(vertx.deploymentIDs().size());
        this.registry = initializeCandidateInformationRegistry(controller.getConfiguration(), candidateNumber);
        this.electionHandler = new LeadershipElectionHandler(candidateNumber, this.registry);
        this.writeHandler = new WriteHandler(controller, messageStore, electionHandler.getCandidateRegistry(), getClient(), getRegistryManager(), vertx);
    }

    // To start simulation, will send a coordination message to do candidate broadcast.
    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        if (message.getMetadata().getRequest().equals(HTTPRequest.UNKNOWN)) {
            Class messageClass = getMessageClass(message.getMetadata());
            if (messageClass == ShortenedExchange.class) {
                log.info("Received state update but not doing anything yet");
                this.writeHandler.handleCoordinationMessage(message);
            } else {
                addCandidateInformation(message);
            }
        } else {
            log.info("Received state update");
            broadcastCandidateNumber();
        }
        updateLeader();
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        writeHandler.handleClientMessage(getClientMessage());
        log.traceExit();
    }

    /**
     * Adds candidate information in message received from other server to candidate registry
     * @param message another server's candidate information
     */
    private void addCandidateInformation(final CoordinationMessage message) {
        log.traceEntry(() -> message);
        log.info("Candidate information message received");
        electionHandler.addCandidateInformation(mapUserDefinitionFromCoordinateMessageToClass(message, CandidateInformation.class));
        broadcastCandidateNumber();
        log.traceExit();
    }

    /**
     * Will send the servers candidate number to all other servers if it has not broadcast to other servers already
     */
    private void broadcastCandidateNumber() {
        log.traceEntry();
        if (hasNotBroadcast) {
            log.info("Broadcasting candidate information to other servers");
            electionHandler.broadcast(registryManager, client, controller);
        }
        hasNotBroadcast = false;
        log.traceExit();
    }

    /**
     * Updates who the server thinks is the leader server if it has received all information from all other servers
     */
    private void updateLeader() {
        log.traceEntry();
        if (electionHandler.canUpdateLeader(registryManager) && controller.getLabel().equals("")) {
            log.info("Updating leader of servers");
            electionHandler.updateLeader(registryManager, controller);
        } else {
            log.info("Cannot update leader as not all candidate information have been received");
        }
        log.traceExit();
    }

    public Class getMessageClass(final CoordinationMetadata metadata) {
        log.traceEntry(() -> metadata);
        final String type = Optional.ofNullable(metadata)
            .map(CoordinationMetadata::getType)
            .orElse("");
        switch (type) {
            case BroadcastCandidateInformationHandler.TYPE:
                return log.traceExit(CandidateInformation.class);
            case LeaderWriteHandler.TYPE:
            case FollowerWriteHandler.TYPE:
                return log.traceExit(ShortenedExchange.class);
        }
        return log.traceExit(Object.class);
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
}
