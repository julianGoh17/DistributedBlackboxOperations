package io.julian.zookeeper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Zxid {
    public final static String EPOCH_KEY = "epoch";
    public final static String COUNTER_KEY = "counter";

    private float epoch;
    private float counter;

    public Zxid(@JsonProperty(EPOCH_KEY) final float epoch,
                @JsonProperty(COUNTER_KEY) final float counter) {
        this.epoch = epoch;
        this.counter = counter;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(EPOCH_KEY, epoch)
            .put(COUNTER_KEY, counter);
    }

}
