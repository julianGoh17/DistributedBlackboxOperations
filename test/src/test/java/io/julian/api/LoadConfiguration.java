package io.julian.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadConfiguration {
    private final static String LOAD_KEY = "load";
    private final static String MODIFIER_KEY = "modifier";
    private final int load;
    private final float modifier;

    public final static LoadConfiguration EXAMPLE = new LoadConfiguration(1, 0.43f);

    public LoadConfiguration(@JsonProperty(LOAD_KEY) final int load,
                             @JsonProperty(MODIFIER_KEY) final float modifier) {
        this.load = load;
        this.modifier = modifier;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(LOAD_KEY, load)
            .put(MODIFIER_KEY, modifier);
    }
}
