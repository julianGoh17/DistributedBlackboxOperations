package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.api.client.ServerClient;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.CandidateInformationTest;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class LeadershipElectionHandlerTest extends AbstractServerBase {
    private final static String HOST = "random-host-1235";
    private final static int PORT = 12346;

    @Test
    public void TestInit() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler(new Configuration(), 0);
        Assert.assertNotEquals(0, handler.getCandidateNumber());
        Assert.assertEquals(1, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestGenerateRandomNumberWithManyDigits() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler(new Configuration(), 0);
        long manyDigitNumber = handler.generateCandidateNumber(0);

        Assert.assertNotEquals(manyDigitNumber, handler.generateCandidateNumber(0));
        Assert.assertTrue(manyDigitNumber > 0);
    }

    @Test
    public void TestAddCandidateInformation() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler(new Configuration(), 0);
        handler.addCandidateInformation(new CandidateInformation(CandidateInformationTest.HOST, CandidateInformationTest.PORT, CandidateInformationTest.CANDIDATE_NUMBER));
        Assert.assertEquals(2, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestUpdateLeaderCorrectlyUpdatesServerToLeader() {
        Controller controller = new Controller(new Configuration());
        RegistryManager manager = new RegistryManager();

        LeadershipElectionHandler handler = new LeadershipElectionHandler(controller.getConfiguration(), 0);
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
        RegistryManager manager = new RegistryManager();
        LeadershipElectionHandler handler = new LeadershipElectionHandler(controller.getConfiguration(), 0);
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
        TestServerComponents serverComponents = setUpApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        TestServerComponents otherServer = setUpApiServer(context, new ServerConfiguration(AbstractServerBase.DEFAULT_SEVER_CONFIG.getHost(), 9898));
        RegistryManager registryManager = createTestRegistryManager();
        registryManager.registerServer(otherServer.configuration.getHost(), otherServer.configuration.getPort());
        ServerClient client = createServerClient();

        LeadershipElectionHandler handler = new LeadershipElectionHandler(new Configuration(), 0);
        Async async = context.async();
        handler.broadcast(registryManager, client, serverComponents.server.getController())
            .onComplete(context.asyncAssertSuccess(res -> {
                context.assertEquals(registryManager.getOtherServers().size(), res.size());
                async.complete();
            }));
        async.awaitSuccess();
        tearDownServer(context, serverComponents);
    }
}
