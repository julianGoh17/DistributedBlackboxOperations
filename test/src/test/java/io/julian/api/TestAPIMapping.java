package io.julian.api;

import io.julian.ExampleDistributedAlgorithm;
import io.julian.server.components.Configuration;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.julian.server.models.HTTPRequest;
import io.julian.server.models.coordination.CoordinationMessage;
import io.julian.server.models.coordination.CoordinationMetadata;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestAPIMapping {
    private Vertx vertx;

    @Before
    public void before() {
        this.vertx = Vertx.vertx();
    }

    @After
    public void after() {
        vertx.close();
    }

    @Test
    public void TestSuccessfulMappingMessage() throws IllegalArgumentException {
        Controller controller = new Controller(new Configuration());
        MessageStore messageStore = new MessageStore();
        ExampleDistributedAlgorithm algorithm = new ExampleDistributedAlgorithm(controller, messageStore, vertx);
        ID mapped = algorithm.mapMessageFromCoordinateMessageToClass(
            new CoordinationMessage(new CoordinationMetadata(HTTPRequest.GET), ID.EXAMPLE.toJson(), LoadConfiguration.EXAMPLE.toJson()),
            ID.class);
        Assert.assertEquals(ID.EXAMPLE.getFirstName(), mapped.getFirstName());
        Assert.assertEquals(ID.EXAMPLE.getLastName(), mapped.getLastName());
        vertx.close();
    }

    @Test
    public void TestFailedMappingMessage() {
        Controller controller = new Controller(new Configuration());
        MessageStore messageStore = new MessageStore();
        ExampleDistributedAlgorithm algorithm = new ExampleDistributedAlgorithm(controller, messageStore, vertx);
        JsonObject jsonThatWillFail = ID.EXAMPLE.toJson().put("random", "key");
        try {
            algorithm.mapMessageFromCoordinateMessageToClass(
                new CoordinationMessage(new CoordinationMetadata(HTTPRequest.GET), jsonThatWillFail, LoadConfiguration.EXAMPLE.toJson()),
                ID.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized field \"random\""));
        }
        vertx.close();
    }

    @Test
    public void TestSuccessfulMappingUserDefinition() throws IllegalArgumentException {
        Controller controller = new Controller(new Configuration());
        MessageStore messageStore = new MessageStore();
        ExampleDistributedAlgorithm algorithm = new ExampleDistributedAlgorithm(controller, messageStore, vertx);
        LoadConfiguration mapped = algorithm.mapUserDefinitionFromCoordinateMessageToClass(
            new CoordinationMessage(new CoordinationMetadata(HTTPRequest.GET), ID.EXAMPLE.toJson(), LoadConfiguration.EXAMPLE.toJson()),
            LoadConfiguration.class);
        Assert.assertEquals(LoadConfiguration.EXAMPLE.getLoad(), mapped.getLoad());
        Assert.assertEquals(LoadConfiguration.EXAMPLE.getModifier(), mapped.getModifier(), 0);
        vertx.close();
    }

    @Test
    public void TestFailedMappingUserDefinition() {
        Controller controller = new Controller(new Configuration());
        MessageStore messageStore = new MessageStore();
        ExampleDistributedAlgorithm algorithm = new ExampleDistributedAlgorithm(controller, messageStore, vertx);
        JsonObject jsonThatWillFail = LoadConfiguration.EXAMPLE.toJson().put("random", "key");
        try {
            algorithm.mapUserDefinitionFromCoordinateMessageToClass(
                new CoordinationMessage(new CoordinationMetadata(HTTPRequest.GET), ID.EXAMPLE.toJson(), jsonThatWillFail),
                LoadConfiguration.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized field \"random\""));
        }
        vertx.close();
    }
}
