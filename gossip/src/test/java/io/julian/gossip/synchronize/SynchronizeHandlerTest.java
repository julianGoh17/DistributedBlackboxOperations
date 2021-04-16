package io.julian.gossip.synchronize;

import io.julian.gossip.components.GossipConfiguration;
import io.julian.gossip.components.State;
import io.julian.gossip.models.MessageUpdate;
import io.julian.gossip.models.SynchronizeUpdate;
import io.julian.server.api.client.RegistryManager;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;
import tools.AbstractHandlerTest;
import tools.TestMetricsCollector;
import tools.TestServerComponents;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SynchronizeHandlerTest extends AbstractHandlerTest {
    private final static String DELETED_ID = "deleted";
    private final static String STORED_ID = "stored";
    private final static JsonObject JSON = new JsonObject();

    @Test
    public void TestGetCoordinateMessage() {
        SynchronizeHandler handler = getSynchronizeHandler(createDefaultState());
        CoordinationMessage message = handler.getCoordinateMessage();
        Assert.assertEquals(HTTPRequest.POST, message.getMetadata().getRequest());
        Assert.assertEquals("synchronize-0", message.getMetadata().getMessageID());
        Assert.assertEquals(SynchronizeHandler.SYNCHRONIZE_TYPE, message.getMetadata().getType());
        Assert.assertNull(message.getMessage());
        Assert.assertEquals(
            new SynchronizeUpdate(Collections.singletonList(new MessageUpdate(STORED_ID, JSON)),
                new HashSet<>(Collections.singletonList(DELETED_ID))).toJson().encodePrettily(),
            message.getDefinition().encodePrettily());
    }

    @Test
    public void TestSuccessfullySendsMessageToSingleServer(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        SynchronizeHandler handler = getSynchronizeHandler(createDefaultState(deadLetters));

        Async async = context.async();

        handler.sendSynchronizeUpdateTo(handler.getCoordinateMessage(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, deadLetters.size());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server);
    }

    @Test
    public void TestFailsToSendMessageToSingleServer(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();
        SynchronizeHandler handler = getSynchronizeHandler(createDefaultState(deadLetters));

        Async async = context.async();

        handler.sendSynchronizeUpdateTo(handler.getCoordinateMessage(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, deadLetters.size());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
    }

    @Test
    public void TestBroadcastServerSucceeds(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server1 = setUpBasicApiServer(context, DEFAULT_SEVER_CONFIG);
        TestServerComponents server2 = setUpBasicApiServer(context, SECOND_SERVER_CONFIG);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();

        RegistryManager manager = createTestRegistryManager();
        manager.registerServer(SECOND_SERVER_CONFIG.getHost(), SECOND_SERVER_CONFIG.getPort());
        SynchronizeHandler handler = getSynchronizeHandler(createDefaultState(deadLetters), manager);

        Async async = context.async();

        handler.sendSynchronizeUpdateTo(handler.getCoordinateMessage(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertSuccess(v -> vertx.setTimer(1000, v1 -> {
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(0, deadLetters.size());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server1);
        tearDownServer(context, server2);
    }

    @Test
    public void TestBroadcastServerFails(final TestContext context) {
        TestMetricsCollector collector = setUpMetricsCollector(context);
        TestServerComponents server = setUpBasicApiServer(context, SECOND_SERVER_CONFIG);
        ConcurrentLinkedQueue<CoordinationMessage> deadLetters = new ConcurrentLinkedQueue<>();

        RegistryManager manager = createTestRegistryManager();
        manager.registerServer(SECOND_SERVER_CONFIG.getHost(), SECOND_SERVER_CONFIG.getPort());
        SynchronizeHandler handler = getSynchronizeHandler(createDefaultState(deadLetters), manager);

        Async async = context.async();

        handler.sendSynchronizeUpdateTo(handler.getCoordinateMessage(), DEFAULT_SEVER_CONFIG)
            .onComplete(context.asyncAssertFailure(cause -> vertx.setTimer(1000, v1 -> {
                Assert.assertEquals(CONNECTION_REFUSED_EXCEPTION, cause.getMessage());
                collector.testHasExpectedStatusSize(1);
                Assert.assertEquals(1, deadLetters.size());
                async.complete();
            })));

        async.awaitSuccess();
        collector.tearDownMetricsCollector(context);
        tearDownServer(context, server);
    }

    private SynchronizeHandler getSynchronizeHandler(final State state) {
        return getSynchronizeHandler(state, createTestRegistryManager());
    }

    private SynchronizeHandler getSynchronizeHandler(final State state, final RegistryManager manager) {
        return new SynchronizeHandler(createServerClient(), state, manager, new GossipConfiguration());
    }

    private State createDefaultState() {
        return createDefaultState(new ConcurrentLinkedQueue<>());
    }

    private State createDefaultState(final ConcurrentLinkedQueue<CoordinationMessage> deadLetter) {
        MessageStore messages = new MessageStore();
        messages.putMessage(STORED_ID, JSON);
        State state = createState(messages, deadLetter);
        state.addDeletedId(DELETED_ID);
        return state;
    }
}
