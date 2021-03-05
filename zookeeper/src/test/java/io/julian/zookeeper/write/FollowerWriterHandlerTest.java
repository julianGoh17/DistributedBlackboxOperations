package io.julian.zookeeper.write;

import io.julian.server.components.Configuration;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.control.ClientMessage;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.zookeeper.AbstractServerBase;
import io.julian.zookeeper.TestServerComponents;
import io.julian.zookeeper.election.CandidateInformationRegistry;
import io.julian.zookeeper.election.LeadershipElectionHandler;
import io.julian.zookeeper.models.CandidateInformation;
import io.julian.zookeeper.models.MessagePhase;
import io.julian.zookeeper.models.ShortenedExchange;
import io.julian.zookeeper.models.Zxid;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

public class FollowerWriterHandlerTest extends AbstractServerBase {
    private final static int EPOCH = 12;
    private final static int COUNTER = 1234;
    private final static Zxid ID = new Zxid(EPOCH, COUNTER);

    @Test
    public void TestAcknowledgeProposalToLeaderIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        server.server.getController().setLabel(LeadershipElectionHandler.LEADER_LABEL);
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();

        Async async = context.async();
        writeHandler.acknowledgeProposalToLeader(ID)
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();

        tearDownServer(context, server);
    }

    @Test
    public void TestAcknowledgeCommitToLeaderIsSuccessful(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        server.server.getController().setLabel(LeadershipElectionHandler.LEADER_LABEL);
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();

        Async async = context.async();
        writeHandler.acknowledgeCommitToLeader(ID)
            .onComplete(context.asyncAssertSuccess(res -> async.complete()));
        async.awaitSuccess();

        tearDownServer(context, server);
    }

    @Test
    public void TestAcknowledgeCommitAndProposalToLeaderFails(final TestContext context) {
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();

        Async async = context.async(2);
        writeHandler.acknowledgeCommitToLeader(ID)
            .onComplete(context.asyncAssertFailure(res -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, res.getMessage());
                async.countDown();
            }));

        writeHandler.acknowledgeProposalToLeader(ID)
            .onComplete(context.asyncAssertFailure(res -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, res.getMessage());
                async.countDown();
            }));

        async.awaitSuccess();
    }

    @Test
    public void TestForwardRequestToLeaderFails(final TestContext context) {
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();

        Async async = context.async();
        writeHandler.forwardRequestToLeader(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""))
            .onComplete(context.asyncAssertFailure(cause -> {
                context.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                async.complete();
            }));
        async.awaitSuccess();
    }

    @Test
    public void TestForwardRequestToLeaderSucceeds(final TestContext context) {
        TestServerComponents server = setUpBasicApiServer(context, AbstractServerBase.DEFAULT_SEVER_CONFIG);
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();

        Async async = context.async();
        writeHandler.forwardRequestToLeader(new ClientMessage(HTTPRequest.POST, new JsonObject(), ""))
            .onComplete(context.asyncAssertSuccess(v -> async.complete()));
        async.awaitSuccess();

        tearDownServer(context, server);
    }

    @Test
    public void TestCreateCoordinationMessage() {
        FollowerWriteHandler writeHandler = createFollowerWriteHandler();
        CoordinationMessage message = writeHandler.createCoordinationMessage(MessagePhase.ACK, ID);
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals(FollowerWriteHandler.ACK_TYPE, message.getMetadata().getType());
        Assert.assertEquals(MessagePhase.ACK.toValue(), message.getDefinition().getString(ShortenedExchange.PHASE_KEY));
        Assert.assertEquals(COUNTER, message.getDefinition()
            .getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.COUNTER_KEY).intValue());
        Assert.assertEquals(EPOCH, message.getDefinition()
            .getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.EPOCH_KEY).intValue());

        message = writeHandler.createCoordinationMessage(MessagePhase.COMMIT, ID);
        Assert.assertEquals(HTTPRequest.UNKNOWN, message.getMetadata().getRequest());
        Assert.assertEquals(FollowerWriteHandler.ACK_TYPE, message.getMetadata().getType());
        Assert.assertEquals(MessagePhase.COMMIT.toValue(), message.getDefinition().getString(ShortenedExchange.PHASE_KEY));
        Assert.assertEquals(COUNTER, message.getDefinition()
            .getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.COUNTER_KEY).intValue());
        Assert.assertEquals(EPOCH, message.getDefinition()
            .getJsonObject(ShortenedExchange.TRANSACTIONAL_ID_KEY).getInteger(Zxid.EPOCH_KEY).intValue());
    }

    private CandidateInformationRegistry createCandidateRegistry() {
        CandidateInformationRegistry registry = new CandidateInformationRegistry();
        registry.addCandidateInformation(new CandidateInformation(Configuration.DEFAULT_SERVER_HOST, Configuration.DEFAULT_SERVER_PORT, 1));
        registry.updateNextLeader();
        return registry;
    }

    private FollowerWriteHandler createFollowerWriteHandler() {
        return new FollowerWriteHandler(createCandidateRegistry(), createServerClient());
    }
}
