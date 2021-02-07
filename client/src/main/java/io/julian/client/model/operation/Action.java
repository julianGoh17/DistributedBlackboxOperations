package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.julian.client.model.RequestMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Action {
    @JsonProperty(value = "method", required = true)
    private RequestMethod method;

    @JsonProperty("messageNumber")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer messageNumber;
}
