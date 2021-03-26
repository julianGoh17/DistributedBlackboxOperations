package io.julian.zookeeper.election;

import io.julian.server.api.client.RegistryManager;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.zookeeper.models.CandidateInformation;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CandidateInformationRegistry {
    private final Logger log = LogManager.getLogger(CandidateInformationRegistry.class);

    private final ConcurrentMap<Long, ServerConfiguration> candidateNumberAndInformationMap = new ConcurrentHashMap<>();
    private final AtomicLong leaderCandidateNumber = new AtomicLong(0);

    /**
     * Adds the information about a candidate into the registry if it doesn't exist
     * @param candidateInformation candidate's information
     */
    public void addCandidateInformation(final CandidateInformation candidateInformation) {
        log.traceEntry(() -> candidateInformation);

        if (candidateNumberAndInformationMap.getOrDefault(candidateInformation.getCandidateNumber(), null) == null) {
            log.info(String.format("Adding candidate information for server at '%s:%d' with candidate number '%d'",
                candidateInformation.getHost(), candidateInformation.getPort(), candidateInformation.getCandidateNumber()));
            candidateNumberAndInformationMap.put(candidateInformation.getCandidateNumber(),
                new ServerConfiguration(candidateInformation.getHost(), candidateInformation.getPort()));
        }
        log.traceExit();
    }

    /**
     * Updates the leader candidate number to the next candidate number bigger than the current candidate number
     */
    public void updateNextLeader() {
        log.traceEntry();
        log.info(String.format("Finding next leader candidate number bigger than '%d'", leaderCandidateNumber.get()));
        ArrayList<Long> candidateNumbers = new ArrayList<>();
        candidateNumberAndInformationMap.forEach((key, v) -> candidateNumbers.add(key));
        Collections.sort(candidateNumbers);
        System.out.println(candidateNumbers);
        for (long num : candidateNumbers) {
            if (num > leaderCandidateNumber.get()) {
                log.info(String.format("Updated leader candidate number '%d'", num));
                leaderCandidateNumber.set(num);
                log.traceExit();
                return;
            }
        }
        log.info(String.format("No bigger candidate number than current number of '%d', updated leader candidate number to smallest number '%d'",
            leaderCandidateNumber.get(), candidateNumbers.get(0)));
        leaderCandidateNumber.set(candidateNumbers.get(0));
        log.traceExit();
    }

    /**
     * Returns the current leader's candidate number
     * @return leader's candidate number
     */
    public long getLeaderCandidateNumber() {
        log.traceEntry();
        return log.traceExit(leaderCandidateNumber.get());
    }

    /**
     * Returns the current leader's server configuration
     * @return the current leader's server configuration
     */
    public ServerConfiguration getLeaderServerConfiguration() {
        log.traceEntry();
        // Return null value in init so we don't have to update the server
        return log.traceExit(candidateNumberAndInformationMap.getOrDefault(leaderCandidateNumber.get(), null));
    }

    /**
     * Checks that the candidate registry is filled with the candidate information of the current server and all the other servers
     * @param registryManager the registry containing other server's host and port
     * @return a boolean determining whether or not the registry is filled
     */
    public boolean isRegistryFilled(final RegistryManager registryManager) {
        log.traceEntry(() -> registryManager);
        return log.traceExit(registryManager.getOtherServers().size() + 1 == candidateNumberAndInformationMap.size());
    }
}
