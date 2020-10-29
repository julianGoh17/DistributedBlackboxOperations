package io.julian.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Action {
    @JsonProperty(value = "method", required = true)
    private RequestMethod method;

    @JsonProperty("messageNumber")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int messageNumber;
}
