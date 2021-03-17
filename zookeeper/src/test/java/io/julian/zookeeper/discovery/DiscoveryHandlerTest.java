package io.julian.zookeeper.discovery;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Proposal;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class DiscoveryHandlerTest extends AbstractServerBase {
    private final static int EPOCH = 12;
    private final static int COUNTER = 2512;
    private final static Proposal PROPOSAL = new Proposal(new ClientMessage(HTTPRequest.POST, new JsonObject(), null), new Zxid(0, 0));
    private final static State STATE = new State(Collections.singletonList(PROPOSAL), 0, EPOCH, COUNTER);

    @Test
    public void TestReset() {
        DiscoveryHandler handler = createDiscoveryHandler(false);
        handler.getLeaderHandler().setLatestState(new State(vertx, new MessageStore()));
        handler.getLeaderHandler().getLatestState().setLeaderEpoch(2);
        handler.getLeaderHandler().getLatestState().setCounter(1);
        Assert.assertFalse(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(0, handler.getState().getCounter());
        Assert.assertEquals(0, handler.getState().getLeaderEpoch());

        handler.reset();
        Assert.assertFalse(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(1, handler.getState().getCounter());
        Assert.assertEquals(2, handler.getState().getLeaderEpoch());
    }
    @Test
    public void TestBroadcastToFollowersSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        DiscoveryHandler handler = createDiscoveryHandler(false);

        Async async = context.async();
        handler.broadcastToFollowers()
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertTrue(handler.hasBroadcastFollowerZXID());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsFollower(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        DiscoveryHandler handler = createDiscoveryHandler(true);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN, "", ""), null, null))
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }


    @Test
    public void TestHandleCoordinationMessageAsFollowerHandlesStateUpdate(final TestContext context) {
        DiscoveryHandler handler = createDiscoveryHandler(true);
        Zxid id = new Zxid(-1, 23);
        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN, "", LeaderDiscoveryHandler.LEADER_STATE_UPDATE_TYPE),
            null,
            id.toJson()))
            .onComplete(context.asyncAssertSuccess(res -> {
                Assert.assertEquals(id.getCounter(), handler.getState().getCounter());
                Assert.assertEquals(id.getEpoch(), handler.getState().getLeaderEpoch());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestHandleCoordinationMessageAsLeaderFails(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        DiscoveryHandler handler = createDiscoveryHandler(false, SECOND_SERVER_CONFIG);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, STATE.toJson()))
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(DiscoveryHandler.NOT_ENOUGH_RESPONSES_ERROR, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsLeaderSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        DiscoveryHandler handler = createDiscoveryHandler(false);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, STATE.toJson()))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageFailsBroadcastingUpdate(final TestContext context) {
        DiscoveryHandler handler = createDiscoveryHandler(false);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, STATE.toJson()))
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    private DiscoveryHandler createDiscoveryHandler(final boolean isFollower, final ServerConfiguration... configurations) {
        CandidateInformationRegistry candidateInformationRegistry = createTestCandidateInformationRegistry(isFollower);
        Controller controller = new Controller(new Configuration());
        controller.setLabel(isFollower ? LeadershipElectionHandler.FOLLOWER_LABEL : LeadershipElectionHandler.LEADER_LABEL);

        RegistryManager manager = createTestRegistryManager();
        for (ServerConfiguration configuration : configurations) manager.registerServer(configuration.getHost(), configuration.getPort());
        return new DiscoveryHandler(controller, new State(vertx, new MessageStore()), candidateInformationRegistry, manager, createServerClient());
    }
}
