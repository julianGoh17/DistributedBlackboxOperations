package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Operation {
    @JsonProperty("action")
    private Action action;
    @JsonProperty("expected")
    private Expected expected;
}
