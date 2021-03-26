package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.CandidateInformationTest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

public class LeadershipElectionHandlerTest extends AbstractServerBase {
    private final static String HOST = "random-host-1235";
    private final static int PORT = 12346;

    @Test
    public void TestInit() {
        LeadershipElectionHandler handler = createTestHandler();
        Assert.assertNotEquals(0, handler.getCandidateNumber());
        Assert.assertEquals(1, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestAddCandidateInformation() {
        LeadershipElectionHandler handler = createTestHandler();
        handler.addCandidateInformation(new CandidateInformation(CandidateInformationTest.HOST, CandidateInformationTest.PORT, CandidateInformationTest.CANDIDATE_NUMBER));
        Assert.assertEquals(2, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestUpdateLeaderCorrectlyUpdatesServerToLeader() {
        Controller controller = new Controller(new Configuration());
        RegistryManager manager = new RegistryManager(new Configuration());

        LeadershipElectionHandler handler = createTestHandler();
        manager.registerServer(HOST, PORT + 234);
        handler.addCandidateInformation(new CandidateInformation(HOST, PORT + 234, (long) (9 * Math.pow(10, 13))));

        Assert.assertEquals("", controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertNull(config.getLabel()));
        handler.updateLeader(manager, controller);

        Assert.assertEquals(LeadershipElectionHandler.LEADER_LABEL, controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertEquals(LeadershipElectionHandler.FOLLOWER_LABEL, config.getLabel()));
    }

    @Test
    public void TestUpdateLeaderCorrectlyUpdatesServerToFollower() {
        Controller controller = new Controller(new Configuration());
        RegistryManager manager = new RegistryManager(new Configuration());

        LeadershipElectionHandler handler = createTestHandler();
        manager.registerServer(HOST, PORT);
        handler.addCandidateInformation(new CandidateInformation(HOST, PORT, 1L));

        Assert.assertEquals("", controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertNull(config.getLabel()));
        handler.updateLeader(manager, controller);

        Assert.assertEquals(LeadershipElectionHandler.FOLLOWER_LABEL, controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertEquals(LeadershipElectionHandler.LEADER_LABEL, config.getLabel()));
    }

    @Test
    public void TestBroadcastIsSuccessful(final TestContext context) {
        TestServerComponents serverComponents = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);

        LeadershipElectionHandler handler = createTestHandler();
        Async async = context.async();
        handler.broadcast()
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();
        tearDownServer(context, serverComponents);
    }

    @Test
    public void TestBroadcastIsUnsuccessful(final TestContext context) {
        LeadershipElectionHandler handler = createTestHandler();
        Async async = context.async();
        handler.broadcast()
            .onComplete(context.asyncAssertFailure(cause -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                Assert.assertEquals(1, handler.getDeadCoordinationMessages().size());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestCreateCoordinationMessages() {
        LeadershipElectionHandler handler = createTestHandler();
        CoordinationMessage message = handler.createCandidateInformationMessage(1L, DEFAULT_SEVER_CONFIG);
        Assert.assertNull(message.getMessage());
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals("candidate_information", message.getMetadata().getType());
        Assert.assertEquals(DEFAULT_SEVER_CONFIG.getHost(), message.getDefinition().getString("host"));
        Assert.assertEquals(DEFAULT_SEVER_CONFIG.getPort(), message.getDefinition().getInteger("port").intValue());
        Assert.assertEquals(1L, message.getDefinition().getLong("candidate_number").longValue());
    }

    private LeadershipElectionHandler createTestHandler() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT, 100L));
        return new LeadershipElectionHandler(100L, registry, createServerClient(), createTestRegistryManager(), new Controller(new Configuration()), new ConcurrentLinkedQueue<>());
    }
}
