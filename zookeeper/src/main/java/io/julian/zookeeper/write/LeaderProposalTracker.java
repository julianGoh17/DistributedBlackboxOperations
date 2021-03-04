package io.julian.zookeeper.write;

import io.julian.zookeeper.models.Zxid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LeaderProposalTracker {
    private static final Logger log = LogManager.getLogger(LeaderProposalTracker.class);
    private final ConcurrentMap<Zxid, Integer> acknowledgedProposals = new ConcurrentHashMap<>();
    private final ConcurrentMap<Zxid, Integer> committedProposals = new ConcurrentHashMap<>();
    private final int majority;

    public LeaderProposalTracker(final int majority) {
        this.majority = majority;
    }

    public void addAcknowledgedProposalTracker(final Zxid id) {
        log.traceEntry(() -> id);
        addProposalTracker(id, acknowledgedProposals);
        log.traceExit();
    }

    public void addCommittedProposalTracker(final Zxid id) {
        log.traceEntry(() -> id);
        addProposalTracker(id, committedProposals);
        log.traceExit();
    }

    public void addAcknowledgedProposal(final Zxid id) {
        log.traceEntry(() -> id);
        addProposal(id, acknowledgedProposals);
        log.traceExit();
    }

    public void addCommittedProposal(final Zxid id) {
        log.traceEntry(() -> id);
        addProposal(id, committedProposals);
        log.traceExit();
    }

    public boolean hasMajorityOfServersAcknowledgedProposal(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(hasMajorityOfServersProposal(id, acknowledgedProposals));
    }

    public boolean hasMajorityOfServersCommittedProposal(final Zxid id) {
        log.traceEntry(() -> id);
        return log.traceExit(hasMajorityOfServersProposal(id, committedProposals));
    }

    public void removeAcknowledgedProposalTracker(final Zxid id) {
        log.traceEntry(() -> id);
        removeProposalTracker(id, acknowledgedProposals);
        log.traceExit();
    }

    public void removeCommittedProposalTracker(final Zxid id) {
        log.traceEntry(() -> id);
        removeProposalTracker(id, committedProposals);
        log.traceExit();
    }

    public void addProposalTracker(final Zxid id, final ConcurrentMap<Zxid, Integer> proposals) {
        log.traceEntry(() -> id);
        log.info(String.format("Attempting to create %s proposal tracker'%s'", mapToName(proposals), id.toString()));
        if (proposals.containsKey(id)) {
            log.info(String.format("Skipping creation of %s proposal tracker for '%s' as it already exists", mapToName(proposals), id.toString()));
        } else {
            log.info(String.format("Created acknowledged %s tracker '%s'", mapToName(proposals), id.toString()));
            proposals.putIfAbsent(id, 0);
        }
        log.traceExit();
    }

    public void addProposal(final Zxid id, final ConcurrentMap<Zxid, Integer> map) {
        log.traceEntry(() -> id, () -> map);
        log.info(String.format("Received %s proposal '%s'", mapToName(map), id.toString()));
        if (map.containsKey(id)) {
            log.info(String.format("Incremented %s proposal tracker '%s'", mapToName(map), id.toString()));
            map.compute(id, (key, val) -> val += 1);
        } else {
            log.info(String.format("Acknowledged %s tracker '%s' has been removed", mapToName(map), id.toString()));
        }
        log.traceExit();
    }

    public boolean hasMajorityOfServersProposal(final Zxid id, final ConcurrentMap<Zxid, Integer> map) {
        log.traceEntry(() -> id, () -> map);
        return log.traceExit(map.getOrDefault(id, 0) >= majority);
    }

    public void removeProposalTracker(final Zxid id, final ConcurrentMap<Zxid, Integer> map) {
        log.traceEntry(() -> id, () -> map);
        log.info(String.format("Attempting to create %s proposal tracker '%s'", mapToName(map), id.toString()));
        if (map.containsKey(id)) {
            log.info(String.format("Removing %s proposal tracker '%s'", mapToName(map), id.toString()));
            map.remove(id);
        } else {
            log.info(String.format("%s proposal tracker '%s' has already been removed", mapToName(map), id.toString()));
        }
        log.traceExit();
    }

    public ConcurrentMap<Zxid, Integer> getAcknowledgedProposals() {
        log.traceEntry();
        return log.traceExit(acknowledgedProposals);
    }

    public ConcurrentMap<Zxid, Integer> getCommittedProposals() {
        log.traceEntry();
        return log.traceExit(committedProposals);
    }

    private String mapToName(final ConcurrentMap<Zxid, Integer> proposals) {
        log.traceEntry(() -> proposals);
        return log.traceExit(proposals == acknowledgedProposals ? "acknowledged" : "committed");
    }
}
