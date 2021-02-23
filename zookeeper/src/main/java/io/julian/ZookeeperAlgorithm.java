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

    public ZookeeperAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
        electionHandler = new LeadershipElectionHandler();
    }

    @Override
    public void actOnCoordinateMessage() {
        log.traceEntry();
        CoordinationMessage message = getCoordinationMessage();
        if (message.getMetadata().getRequest().equals(HTTPRequest.UNKNOWN)) {
            log.info("Candidate information message received");
            electionHandler.addCandidateInformation(mapUserDefinitionFromCoordinateMessageToClass(message, CandidateInformation.class));
        } else {
            log.info("Ignoring as currently have not implemented write");
        }
        log.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        log.traceEntry();
        log.traceExit();
    }
}
