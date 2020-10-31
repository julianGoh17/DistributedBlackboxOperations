package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    @JsonProperty("willRunInParallel")
    private boolean willRunInParallel;
    @JsonProperty("willFailFast")
    private boolean willFailFast;

    public boolean willRunInParallel() {
        return willRunInParallel;
    }

    public boolean willFailFast() {
        return willFailFast;
    }
}
