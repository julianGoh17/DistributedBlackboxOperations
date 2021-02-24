package io.julian.zookeeper;

import io.julian.server.api.client.ServerClient;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ServerConfiguration;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IntegrationTest extends AbstractServerBase {
    @Test
    public void TestServersSelectLeader(final TestContext context) {
        TestServerComponents server1 = setUpApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpApiServer(context, new ServerConfiguration(AbstractServerBase.DEFAULT_SEVER_CONFIG.getHost(), 9898));

        server1.server.getVerticle().getAlgorithm().getRegistryManager().registerServer(AbstractServerBase.DEFAULT_SEVER_CONFIG.getHost(), 9898);
        server2.server.getVerticle().getAlgorithm().getRegistryManager().registerServer(AbstractServerBase.DEFAULT_SEVER_CONFIG.getHost(),
            AbstractServerBase.DEFAULT_SEVER_CONFIG.getPort());
        ServerClient client = createServerClient();

        Async async = context.async();
        client.sendCoordinateMessageToServer(AbstractServerBase.DEFAULT_SEVER_CONFIG, new CoordinationMessage(HTTPRequest.POST, new JsonObject()))
            .onComplete(context.asyncAssertSuccess(res ->
                // Wait 2 seconds to let servers stabilize
                vertx.setTimer(1000, complete -> async.complete())));
        async.awaitSuccess();
        List<String> labels = Arrays.asList(server2.server.getController().getLabel(), server1.server.getController().getLabel());
        Assert.assertEquals(1,
            labels.stream().filter(label -> label.equals(LeadershipElectionHandler.LEADER_LABEL)).count());
        Assert.assertEquals(1,
            labels.stream().filter(label -> label.equals(LeadershipElectionHandler.FOLLOWER_LABEL)).count());
        tearDownServer(context, server1);
        tearDownServer(context, server2);
    }
}
