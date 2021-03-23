package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.Configuration;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.models.CandidateInformation;
import org.junit.Assert;
import org.junit.Test;

public class CandidateInformationRegistryTest {
    private final static String HOST = "host-1234";
    private final static int PORT = 999;
    private final static long CANDIDATE_NUMBER = 99999L;

    @Test
    public void TestInit() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        Assert.assertEquals(0, registry.getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestAddCandidateInformation() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();

        registry.addCandidateInformation(new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER));
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());

        ServerConfiguration config = registry.getCandidateNumberAndInformationMap().getOrDefault(CANDIDATE_NUMBER, null);
        Assert.assertNotNull(config);
        Assert.assertEquals(HOST, config.getHost());
        Assert.assertEquals(PORT, config.getPort());
    }

    @Test
    public void TestAddCandidateInformationDoesNotAddIfInformationAlreadyExists() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();

        registry.addCandidateInformation(new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER));
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());
        ServerConfiguration config = registry.getCandidateNumberAndInformationMap().getOrDefault(CANDIDATE_NUMBER, null);
        Assert.assertNotNull(config);
        Assert.assertEquals(HOST, config.getHost());
        Assert.assertEquals(PORT, config.getPort());

        registry.addCandidateInformation(new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER));
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestAddServer() {
        String higherHost = "higher-host";
        int higherPort = 9999;
        long higherCandidateNumber = 1112L;

        CandidateInformationRegistry registry = new CandidateInformationRegistry();

        registry.addCandidateInformation(new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER));
        registry.addCandidateInformation(new CandidateInformation(higherHost, higherPort, higherCandidateNumber));
        Assert.assertEquals(2, registry.getCandidateNumberAndInformationMap().size());

        registry.updateNextLeader();
        Assert.assertEquals(higherCandidateNumber, registry.getLeaderCandidateNumber());
        // Should reset at this point as at end of list
        registry.updateNextLeader();
        Assert.assertEquals(CANDIDATE_NUMBER, registry.getLeaderCandidateNumber());
    }

    @Test
    public void TestGetLeaderServerConfiguration() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        Assert.assertEquals(0, registry.getCandidateNumberAndInformationMap().size());
        CandidateInformation info = new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER);
        registry.addCandidateInformation(info);
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());

        Assert.assertNull(registry.getLeaderServerConfiguration());
        registry.updateNextLeader();
        Assert.assertEquals(info.getHost(), registry.getLeaderServerConfiguration().getHost());
        Assert.assertEquals(info.getPort(), registry.getLeaderServerConfiguration().getPort());
    }

    @Test
    public void TestIsRegistryFilled() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        RegistryManager manager = new RegistryManager(new Configuration());

        Assert.assertFalse(registry.isRegistryFilled(manager));

        registry.addCandidateInformation(new CandidateInformation(HOST, PORT, CANDIDATE_NUMBER));
        Assert.assertTrue(registry.isRegistryFilled(manager));
    }
}
