package io.julian;

import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.CandidateInformation;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZookeeperAlgorithm extends DistributedAlgorithm {
    private final Logger log = LogManager.getLogger(ZookeeperAlgorithm.class);
    private final LeadershipElectionHandler electionHandler;
    private boolean hasNotBroadcast = true;

    public ZookeeperAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
        electionHandler = new LeadershipElectionHandler(controller.getConfiguration(), vertx.deploymentIDs().size());
    }

    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        if (message.getMetadata().getRequest().equals(HTTPRequest.UNKNOWN)) {
            addCandidateInformation(message);
        } else {
            log.info("Ignoring as currently have not implemented write");
            broadcastCandidateNumber();
        }
        updateLeader();
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
        if (electionHandler.canUpdateLeader(registryManager)) {
            log.info("Updating leader of servers");
            electionHandler.updateLeader(registryManager, controller);
        } else {
            log.info("Cannot update leader as not all candidate information have been received");
        }
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        log.traceExit();
    }
}
