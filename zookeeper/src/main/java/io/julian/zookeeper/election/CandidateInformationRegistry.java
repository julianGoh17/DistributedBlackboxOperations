package io.julian.zookeeper.election;

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
    private final Logger logger = LogManager.getLogger(CandidateInformationRegistry.class);

    private final ConcurrentMap<Long, ServerConfiguration> candidateNumberAndInformationMap = new ConcurrentHashMap<>();
    private final AtomicLong leaderCandidateNumber = new AtomicLong(0);

    public void addCandidateInformation(final CandidateInformation candidateInformation) {
        logger.traceEntry(() -> candidateInformation);
        logger.info(String.format("Adding candidate information for server at '%s:%d'", candidateInformation.getHost(), candidateInformation.getPort()));
        candidateNumberAndInformationMap.put(candidateInformation.getCandidateNumber(),
            new ServerConfiguration(candidateInformation.getHost(), candidateInformation.getPort()));
        logger.traceExit();
    }

    public void updateNextLeader() {
        logger.traceEntry();
        logger.info(String.format("Finding next leader candidate number bigger than '%d'", leaderCandidateNumber.get()));
        ArrayList<Long> candidateNumbers = new ArrayList<>();
        candidateNumberAndInformationMap.forEach((key, v) -> candidateNumbers.add(key));
        Collections.sort(candidateNumbers);
        for (long num : candidateNumbers) {
            if (num > leaderCandidateNumber.get()) {
                logger.info(String.format("Updated leader candidate number '%d'", num));
                leaderCandidateNumber.set(num);
                return;
            }
        }
        logger.info(String.format("No bigger candidate number than current number of '%d', updated leader candidate number to smallest number '%d'",
            leaderCandidateNumber.get(), candidateNumbers.get(0)));
        leaderCandidateNumber.set(candidateNumbers.get(0));
    }

    public long getLeaderCandidateNumber() {
        logger.traceEntry();
        return logger.traceExit(leaderCandidateNumber.get());
    }
}
