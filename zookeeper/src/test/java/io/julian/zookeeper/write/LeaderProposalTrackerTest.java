package io.julian.zookeeper.write;

import io.julian.zookeeper.models.Zxid;
import io.julian.zookeeper.models.ZxidTest;
import io.vertx.core.json.JsonObject;
import org.junit.Assert;
import org.junit.Test;

public class LeaderProposalTrackerTest {
    private final static int MAJORITY = 2;
    private final static JsonObject ID_JSON = ZxidTest.JSON;

    @Test
    public void TestInit() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(0, tracker.getCommittedProposals().size());
    }

    @Test
    public void TestAddAcknowledgedProposalTrackerIsSuccessful() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());

        Zxid first = createZxid();
        Zxid second = createZxid();

        tracker.addAcknowledgedProposalTracker(first);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().getOrDefault(first, 0).intValue());
        tracker.addAcknowledgedProposalTracker(second);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().getOrDefault(first, 0).intValue());
    }

    @Test
    public void TestAddAcknowledgedProposalSuccessfullyAddsZxid() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid first = createZxid();
        Zxid second = createZxid();

        tracker.addAcknowledgedProposalTracker(first);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().getOrDefault(first, 0).intValue());

        tracker.addAcknowledgedProposal(first);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().getOrDefault(first, 0).intValue());

        tracker.addAcknowledgedProposal(second);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(2, tracker.getAcknowledgedProposals().getOrDefault(first, 0).intValue());
    }

    @Test
    public void TestAddAcknowledgedProposalDoesNotAddIfNoEntry() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid first = createZxid();
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());

        tracker.addAcknowledgedProposal(first);
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());
    }

    @Test
    public void TestHasMajorityOfServersAcknowledgedProposal() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid id = createZxid();

        Assert.assertFalse(tracker.hasMajorityOfServersAcknowledgedProposal(id));

        tracker.addAcknowledgedProposalTracker(id);
        Assert.assertFalse(tracker.hasMajorityOfServersAcknowledgedProposal(id));

        tracker.addAcknowledgedProposal(id);
        Assert.assertFalse(tracker.hasMajorityOfServersAcknowledgedProposal(id));

        tracker.addAcknowledgedProposal(id);
        Assert.assertTrue(tracker.hasMajorityOfServersAcknowledgedProposal(id));
    }

    @Test
    public void TestRemoveAcknowledgedProposalEntry() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid id = createZxid();

        tracker.removeAcknowledgedProposalTracker(id);
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());

        tracker.addAcknowledgedProposalTracker(id);
        tracker.addAcknowledgedProposal(id);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        tracker.removeAcknowledgedProposalTracker(id);
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());
    }

    @Test
    public void TestAddCommittedProposalTrackerIsSuccessful() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Assert.assertEquals(0, tracker.getCommittedProposals().size());

        Zxid first = createZxid();
        Zxid second = createZxid();

        tracker.addCommittedProposalTracker(first);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        Assert.assertEquals(0, tracker.getCommittedProposals().getOrDefault(first, 0).intValue());

        tracker.addCommittedProposalTracker(second);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        Assert.assertEquals(0, tracker.getCommittedProposals().getOrDefault(first, 0).intValue());
    }

    @Test
    public void TestAddCommittedProposalSuccessfullyAddsZxid() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid first = createZxid();
        Zxid second = createZxid();

        tracker.addCommittedProposalTracker(first);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        Assert.assertEquals(0, tracker.getCommittedProposals().getOrDefault(first, 0).intValue());

        tracker.addCommittedProposal(first);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        Assert.assertEquals(1, tracker.getCommittedProposals().getOrDefault(first, 0).intValue());

        tracker.addCommittedProposal(second);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        Assert.assertEquals(2, tracker.getCommittedProposals().getOrDefault(first, 0).intValue());
    }

    @Test
    public void TestAddCommittedProposalDoesNotAddIfNoEntry() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid first = createZxid();
        Assert.assertEquals(0, tracker.getCommittedProposals().size());

        tracker.addCommittedProposal(first);
        Assert.assertEquals(0, tracker.getCommittedProposals().size());
    }

    @Test
    public void TestHasMajorityOfServersCommittedProposal() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid id = createZxid();

        Assert.assertFalse(tracker.hasMajorityOfServersCommittedProposal(id));

        tracker.addCommittedProposalTracker(id);
        Assert.assertFalse(tracker.hasMajorityOfServersCommittedProposal(id));

        tracker.addCommittedProposal(id);
        Assert.assertFalse(tracker.hasMajorityOfServersCommittedProposal(id));

        tracker.addCommittedProposal(id);
        Assert.assertTrue(tracker.hasMajorityOfServersCommittedProposal(id));
    }

    @Test
    public void TestRemoveCommittedProposalEntry() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid id = createZxid();

        tracker.removeCommittedProposalTracker(id);
        Assert.assertEquals(0, tracker.getCommittedProposals().size());

        tracker.addCommittedProposalTracker(id);
        tracker.addCommittedProposal(id);
        Assert.assertEquals(1, tracker.getCommittedProposals().size());
        tracker.removeCommittedProposalTracker(id);
        Assert.assertEquals(0, tracker.getCommittedProposals().size());
    }

    @Test
    public void TestReset() {
        LeaderProposalTracker tracker = new LeaderProposalTracker(MAJORITY);
        Zxid id = createZxid();
        tracker.addCommittedProposalTracker(id);
        tracker.addAcknowledgedProposalTracker(id);
        Assert.assertEquals(1, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(1, tracker.getCommittedProposals().size());

        tracker.reset();
        Assert.assertEquals(0, tracker.getAcknowledgedProposals().size());
        Assert.assertEquals(0, tracker.getCommittedProposals().size());
    }

    private Zxid createZxid() {
        return ID_JSON.mapTo(Zxid.class);
    }
}
