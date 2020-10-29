package io.julian.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Operation {
    @JsonProperty("action")
    private Action action;
    @JsonProperty("expected")
    private Expected expected;
}
