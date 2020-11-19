package io.julian.server.models.coordination;

import io.vertx.core.json.JsonObject;

public abstract class AbstractCoordinationUserDefinition {
    public abstract JsonObject toJson();
}
