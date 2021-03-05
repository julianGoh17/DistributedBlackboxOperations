package io.julian.zookeeper.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Zxid {
    public final static String EPOCH_KEY = "epoch";
    public final static String COUNTER_KEY = "counter";

    private int epoch;
    private int counter;

    public Zxid(@JsonProperty(EPOCH_KEY) final int epoch,
                @JsonProperty(COUNTER_KEY) final int counter) {
        this.epoch = epoch;
        this.counter = counter;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(EPOCH_KEY, epoch)
            .put(COUNTER_KEY, counter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.epoch, this.counter);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Zxid) {
            Zxid id = (Zxid) obj;
            return id.counter == this.counter && id.epoch == this.epoch;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("(epoch: %d, counter: %d)", this.epoch, this.counter);
    }
}
