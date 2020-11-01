package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    @JsonProperty("willRunInParallel")
    private boolean willRunInParallel;

    public boolean willRunInParallel() {
        return willRunInParallel;
    }
}
