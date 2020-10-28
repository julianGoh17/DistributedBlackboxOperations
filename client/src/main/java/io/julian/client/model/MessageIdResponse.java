package io.julian.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageIdResponse extends AbstractResponse {
    @JsonProperty("messageId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String messageId;
}
