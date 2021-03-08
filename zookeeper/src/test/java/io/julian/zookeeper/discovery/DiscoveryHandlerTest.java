package io.julian.zookeeper.discovery;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.controller.State;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.Zxid;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class DiscoveryHandlerTest extends AbstractServerBase {
    private final static int EPOCH = 12;
    private final static int COUNTER = 2512;
    private final static Zxid ID = new Zxid(EPOCH, COUNTER);

    @Test
    public void TestResetAsFollower() {
        DiscoveryHandler handler = createDiscoveryHandler(true);
        handler.getState().setCounter(1);
        handler.getState().setLeaderEpoch(2);
        Assert.assertFalse(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(0, handler.getLeaderHandler().getCounter());
        Assert.assertEquals(0, handler.getLeaderHandler().getEpoch());

        handler.reset();
        Assert.assertTrue(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(1, handler.getLeaderHandler().getCounter());
        Assert.assertEquals(2, handler.getLeaderHandler().getEpoch());
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
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, null))
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestHandleCoordinationMessageAsLeaderFails(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        DiscoveryHandler handler = createDiscoveryHandler(false, SECOND_SERVER_CONFIG);

        Async async = context.async();
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, ID.toJson()))
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
        handler.handleCoordinationMessage(new CoordinationMessage(new CoordinationMetadata(HTTPRequest.UNKNOWN), null, ID.toJson()))
            .onComplete(context.asyncAssertSuccess(cause -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, server);
    }

    @Test
    public void TestResetAsLeader() {
        DiscoveryHandler handler = createDiscoveryHandler(false);
        handler.getState().setCounter(1);
        handler.getState().setLeaderEpoch(2);
        Assert.assertFalse(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(0, handler.getLeaderHandler().getCounter());
        Assert.assertEquals(0, handler.getLeaderHandler().getEpoch());

        handler.reset();
        Assert.assertFalse(handler.hasBroadcastFollowerZXID());
        Assert.assertEquals(1, handler.getLeaderHandler().getCounter());
        Assert.assertEquals(2, handler.getLeaderHandler().getEpoch());
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
