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
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class LeadershipElectionHandlerTest extends AbstractServerBase {
    private final static String HOST = "random-host-1235";
    private final static int PORT = 12346;

    @Test
    public void TestInit() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler();
        Assert.assertNotEquals(0, handler.getCandidateNumber());
        Assert.assertEquals(1, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestGenerateRandomNumberWithManyDigits() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler();
        long manyDigitNumber = handler.generateRandomNumberWithManyDigits();

        Assert.assertNotEquals(manyDigitNumber, handler.generateRandomNumberWithManyDigits());
        Assert.assertTrue(manyDigitNumber > 0);
    }

    @Test
    public void TestAddCandidateInformation() {
        LeadershipElectionHandler handler = new LeadershipElectionHandler();
        handler.addCandidateInformation(new CandidateInformation(CandidateInformationTest.HOST, CandidateInformationTest.PORT, CandidateInformationTest.CANDIDATE_NUMBER));
        Assert.assertEquals(2, handler.getCandidateRegistry().getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestUpdateLeaderCorrectlyUpdatesServerToLeader() {
        Controller controller = new Controller();
        RegistryManager manager = new RegistryManager();

        LeadershipElectionHandler handler = new LeadershipElectionHandler();
        manager.registerServer(HOST, PORT);
        handler.addCandidateInformation(new CandidateInformation(HOST, PORT, (long) (9 * Math.pow(10, 13))));

        Assert.assertEquals("", controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertNull(config.getLabel()));
        handler.updateLeader(manager, controller);

        Assert.assertEquals(LeadershipElectionHandler.LEADER_LABEL, controller.getLabel());
        manager.getOtherServers().forEach(config -> Assert.assertEquals(LeadershipElectionHandler.FOLLOWER_LABEL, config.getLabel()));
    }

    @Test
    public void TestUpdateLeaderCorrectlyUpdatesServerToFollower() {
        Controller controller = new Controller();
        RegistryManager manager = new RegistryManager();
        LeadershipElectionHandler handler = new LeadershipElectionHandler();
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
        TestServerComponents otherServer = setUpApiServer(context, new ServerConfiguration(AbstractServerBase.DEFAULT_SEVER_CONFIG.getHost(), 9999));
        RegistryManager registryManager = createTestRegistryManager();
        registryManager.registerServer(otherServer.configuration.getHost(), otherServer.configuration.getPort());
        ServerClient client = createServerClient();

        LeadershipElectionHandler handler = new LeadershipElectionHandler();
        handler.broadcast(registryManager, client, serverComponents.server.getController())
            .onComplete(context.asyncAssertSuccess(res -> context.assertEquals(registryManager.getOtherServers().size(), res.size())));

        tearDownServer(context, serverComponents);
    }

    private RegistryManager createTestRegistryManager() {
        RegistryManager manager = new RegistryManager();
        manager.registerServer(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT);
        return manager;
    }

    private ServerClient createServerClient() {
        return new ServerClient(this.vertx);
    }
}
