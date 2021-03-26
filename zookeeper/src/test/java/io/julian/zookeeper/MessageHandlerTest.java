package io.julian.zookeeper;

import io.julian.MessageHandler;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.Stage;
import io.julian.zookeeper.synchronize.SynchronizeHandler;
import io.julian.zookeeper.write.LeaderWriteHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandlerTest extends AbstractServerBase {
    @Test
    public void TestGenerateRandomNumberWithManyDigits() {
        MessageHandler handler = createTestHandler();
        long manyDigitNumber = handler.generateCandidateNumber(0);

        Assert.assertNotEquals(manyDigitNumber, handler.generateCandidateNumber(0));
        Assert.assertTrue(manyDigitNumber > 0);
    }

    @Test
    public void TestInitializeCandidateRegistry() {
        MessageHandler handler = createTestHandler();
        CandidateInformationRegistry registry = handler.initializeCandidateInformationRegistry(new Configuration(), 1);
        Assert.assertEquals(1, registry.getCandidateNumberAndInformationMap().size());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_HOST, registry.getCandidateNumberAndInformationMap().get(1L).getHost());
        Assert.assertEquals(Configuration.DEFAULT_SERVER_PORT, registry.getCandidateNumberAndInformationMap().get(1L).getPort());
    }

    @Test
    public void TestUpdateLeaderUpdatesCurrentServerToLeader() {
        MessageHandler handler = createTestHandler();
        long candidateNumber = new ArrayList<>(handler.getRegistry().getCandidateNumberAndInformationMap().keySet()).get(0);
        handler.getRegistry().addCandidateInformation(new CandidateInformation("", 0, candidateNumber + 1));
        Assert.assertEquals(Stage.SETUP, handler.getState().getServerStage());
        handler.updateLeader();
        Assert.assertEquals(Stage.DISCOVERY, handler.getState().getServerStage());
        Assert.assertEquals(handler.getController().getConfiguration().getServerHost(), handler.getRegistry().getLeaderServerConfiguration().getHost());
        Assert.assertEquals(handler.getController().getConfiguration().getServerPort(), handler.getRegistry().getLeaderServerConfiguration().getPort());
    }

    @Test
    public void TestUpdateLeaderUpdatesCurrentServerToFollower() {
        MessageHandler handler = createTestHandler();
        long candidateNumber = new ArrayList<>(handler.getRegistry().getCandidateNumberAndInformationMap().keySet()).get(0);
        handler.getRegistry().addCandidateInformation(new CandidateInformation("", 0, candidateNumber - 1));
        Assert.assertEquals(Stage.SETUP, handler.getState().getServerStage());
        handler.updateLeader();
        Assert.assertEquals(Stage.DISCOVERY, handler.getState().getServerStage());
        Assert.assertNotEquals(handler.getController().getConfiguration().getServerHost(), handler.getRegistry().getLeaderServerConfiguration().getHost());
        Assert.assertNotEquals(handler.getController().getConfiguration().getServerPort(), handler.getRegistry().getLeaderServerConfiguration().getPort());
    }

    @Test
    public void TestUpdateFollowerState() {
        MessageHandler handler = createTestHandler();
        handler.getState().setServerStage(Stage.DISCOVERY);
        handler.getController().setLabel(LeadershipElectionHandler.FOLLOWER_LABEL);

        CoordinationMessage invalidMessage = new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", "invalid-type"), null, null);
        CoordinationMessage synchronize = new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", SynchronizeHandler.SYNCHRONIZE_TYPE), null, null);
        CoordinationMessage write = new CoordinationMessage(new CoordinationMetadata(HTTPRequest.POST, "", LeaderWriteHandler.TYPE), null, null);

        handler.updateFollowerState(invalidMessage);
        Assert.assertEquals(Stage.DISCOVERY, handler.getState().getServerStage());
        handler.updateFollowerState(write);
        Assert.assertEquals(Stage.DISCOVERY, handler.getState().getServerStage());
        handler.updateFollowerState(synchronize);
        Assert.assertEquals(Stage.SYNCHRONIZE, handler.getState().getServerStage());

        handler.getState().setServerStage(Stage.SYNCHRONIZE);
        handler.updateFollowerState(invalidMessage);
        Assert.assertEquals(Stage.SYNCHRONIZE, handler.getState().getServerStage());
        handler.updateFollowerState(synchronize);
        Assert.assertEquals(Stage.SYNCHRONIZE, handler.getState().getServerStage());
        handler.updateFollowerState(write);
        Assert.assertEquals(Stage.WRITE, handler.getState().getServerStage());
    }

    @Test
    public void TestCanChangeStateTo() {
        MessageHandler handler = createTestHandler();
        for (Stage stage : Stage.values()) {
            Assert.assertFalse(handler.canChangeStateTo(stage, Stage.SYNCHRONIZE, "wrong-type"));
            if (stage.equals(Stage.DISCOVERY)) {
                Assert.assertFalse(handler.canChangeStateTo(stage, Stage.SYNCHRONIZE, SynchronizeHandler.SYNCHRONIZE_TYPE));
            }
        }
        Assert.assertFalse(handler.canChangeStateTo(Stage.DISCOVERY, Stage.SYNCHRONIZE, SynchronizeHandler.SYNCHRONIZE_TYPE));
    }

    private MessageHandler createTestHandler() {
        return new MessageHandler(new Controller(new Configuration()), new MessageStore(), this.vertx, createTestRegistryManager(), createServerClient(), new ConcurrentLinkedQueue<>());
    }
}
