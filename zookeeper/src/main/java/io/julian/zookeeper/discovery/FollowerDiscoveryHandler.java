package io.julian.zookeeper.discovery;

import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FollowerDiscoveryHandler {
    public final static String ZXID_FOLLOWER_TYPE = "ZXID_FOLLOWER";

    private final static Logger log = LogManager.getLogger(FollowerDiscoveryHandler.class);
    private final State state;
    private final CandidateInformationRegistry registry;
    private final ServerClient client;

    public FollowerDiscoveryHandler(final State state, final CandidateInformationRegistry registry, final ServerClient client) {
        this.state = state;
        this.registry = registry;
        this.client = client;
    }

    public Future<Void> replyToLeader() {
        log.traceEntry();
        final Zxid id = new Zxid(state.getLeaderEpoch(), state.getCounter());
        log.info(String.format("Sending leader latest ZXID %s", id));
        Promise<Void> post = Promise.promise();
        client.sendCoordinateMessageToServer(registry.getLeaderServerConfiguration(), createCoordinationMessage(id))
            .onSuccess(v -> {
                log.info(String.format("Successfully sent leader latest ZXID %s", id));
                post.complete();
            })
            .onFailure(cause -> {
                log.info(String.format("Unsuccessfully sent leader latest ZXID %s", id));
                log.error(cause);
                post.fail(cause);
            });
        return log.traceExit(post.future());
    }

    public CoordinationMessage createCoordinationMessage(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(new CoordinationMessage(
            new CoordinationMetadata(HTTPRequest.UNKNOWN, "", ZXID_FOLLOWER_TYPE),
            null,
            id.toJson()));
    }
}
