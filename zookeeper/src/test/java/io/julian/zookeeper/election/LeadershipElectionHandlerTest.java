package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.Controller;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.CandidateInformationTest;
import org.junit.Assert;
import org.junit.Test;

public class LeadershipElectionHandlerTest {
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
}
