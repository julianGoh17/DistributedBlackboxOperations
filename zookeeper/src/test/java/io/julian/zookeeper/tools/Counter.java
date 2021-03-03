package io.julian.zookeeper.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

public class Counter {
    public final static String COUNT_KEY = "COUNT";
    private final int count;

    public Counter(@JsonProperty(COUNT_KEY) final int count) {
        this.count = count;
    }

    public JsonObject toJson() {
        return new JsonObject().put(COUNT_KEY, count);
    }

    public int getCount() {
        return count;
    }
}
