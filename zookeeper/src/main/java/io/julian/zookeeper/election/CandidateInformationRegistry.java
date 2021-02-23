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

    public void addCandidateInformation(final CandidateInformation candidateInformation) {
        log.traceEntry(() -> candidateInformation);
        log.info(String.format("Adding candidate information for server at '%s:%d'", candidateInformation.getHost(), candidateInformation.getPort()));
        candidateNumberAndInformationMap.put(candidateInformation.getCandidateNumber(),
            new ServerConfiguration(candidateInformation.getHost(), candidateInformation.getPort()));
        log.traceExit();
    }

    public void updateNextLeader() {
        log.traceEntry();
        log.info(String.format("Finding next leader candidate number bigger than '%d'", leaderCandidateNumber.get()));
        ArrayList<Long> candidateNumbers = new ArrayList<>();
        candidateNumberAndInformationMap.forEach((key, v) -> candidateNumbers.add(key));
        Collections.sort(candidateNumbers);
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

    public long getLeaderCandidateNumber() {
        log.traceEntry();
        return log.traceExit(leaderCandidateNumber.get());
    }

    public ServerConfiguration getLeaderServerConfiguration() {
        log.traceEntry();
        // Return null value in init so we don't have to update the server
        return log.traceExit(candidateNumberAndInformationMap.getOrDefault(leaderCandidateNumber.get(), null));
    }

    public boolean isRegistryFilled(final RegistryManager registryManager) {
        log.traceEntry(() -> registryManager);
        return log.traceExit(registryManager.getOtherServers().size() + 1 == candidateNumberAndInformationMap.size());
    }
}
