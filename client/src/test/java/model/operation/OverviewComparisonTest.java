package model.operation;

import io.julian.client.model.operation.OverviewComparison;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class OverviewComparisonTest {
    private final static String HOST = "localhost";
    private final static int PORT = 9999;

    @Test
    public void TestInit() {
        OverviewComparison overviewComparison = new OverviewComparison(HOST, PORT);
        Assert.assertEquals(HOST, overviewComparison.getHost());
        Assert.assertEquals(PORT, overviewComparison.getPort());
        Assert.assertEquals(0, overviewComparison.getMissingIdsInClient().size());
        Assert.assertEquals(0, overviewComparison.getMissingIdsInServer().size());
        Assert.assertNotNull(overviewComparison.getTimestamp());
    }

    @Test
    public void TestOverviewDoesNotChangeWhenEqualListsAdded() {
        OverviewComparison overviewComparison = new OverviewComparison(HOST, PORT);
        List<String> list = Collections.singletonList("random");
        overviewComparison.compareClientExpectedStateToServerOverview(list, list);
        Assert.assertEquals(0, overviewComparison.getMissingIdsInClient().size());
        Assert.assertEquals(0, overviewComparison.getMissingIdsInServer().size());
    }

    @Test
    public void TestOverviewChangesWhenNotEqualListsAdded() {
        OverviewComparison overviewComparison = new OverviewComparison(HOST, PORT);
        List<String> serverIds = Collections.singletonList("server");
        List<String> clientIds = Collections.singletonList("client");
        overviewComparison.compareClientExpectedStateToServerOverview(clientIds, serverIds);
        Assert.assertEquals(1, overviewComparison.getMissingIdsInClient().size());
        Assert.assertEquals(serverIds.get(0), overviewComparison.getMissingIdsInClient().get(0));
        Assert.assertEquals(1, overviewComparison.getMissingIdsInServer().size());
        Assert.assertEquals(clientIds.get(0), overviewComparison.getMissingIdsInServer().get(0));
    }
}
