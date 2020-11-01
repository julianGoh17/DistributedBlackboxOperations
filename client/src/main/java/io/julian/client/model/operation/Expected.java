package io.julian.client.model.operation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Expected {
    @JsonProperty("statusCode")
    private int statusCode;

    @JsonProperty("messageNumber")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer messageNumber;
}