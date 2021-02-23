package io.julian.zookeeper.election;

import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.models.CandidateInformation;
import org.junit.Assert;
import org.junit.Test;

public class CandidateInformationRegistryTest {
    @Test
    public void TestInit() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        Assert.assertEquals(0, registry.getCandidateNumberAndInformationMap().size());
    }

    @Test
    public void TestAddCandidateInformation() {
        String host = "host-1234";
        int port = 999;
        long candidateNumber = 99999L;

        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        Assert.assertEquals(0, registry.getCandidateNumberAndInformationMap().size());

        registry.addCandidateInformation(new CandidateInformation(host, port, candidateNumber));
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());

        ServerConfiguration config = registry.getCandidateNumberAndInformationMap().getOrDefault(candidateNumber, null);
        Assert.assertNotNull(config);
        Assert.assertEquals(host, config.getHost());
        Assert.assertEquals(port, config.getPort());
    }

    @Test
    public void TestAddServer() {
        String lowerHost = "host-1234";
        int lowerPort = 999;
        long lowerCandidateNumber = 0L;

        String higherHost = "higher-host";
        int higherPort = 9999;
        long higherCandidateNumber = 1112L;

        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        Assert.assertEquals(0, registry.getCandidateNumberAndInformationMap().size());

        registry.addCandidateInformation(new CandidateInformation(lowerHost, lowerPort, lowerCandidateNumber));
        registry.addCandidateInformation(new CandidateInformation(higherHost, higherPort, higherCandidateNumber));
        Assert.assertEquals(2, registry.getCandidateNumberAndInformationMap().size());

        registry.updateNextLeader();
        Assert.assertEquals(higherCandidateNumber, registry.getLeaderCandidateNumber());
        // Should reset at this point as at end of list
        registry.updateNextLeader();
        Assert.assertEquals(lowerCandidateNumber, registry.getLeaderCandidateNumber());
    }
}
