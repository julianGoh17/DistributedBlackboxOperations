package io.julian.client.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Setter
public class GetMessageResponse extends AbstractResponse {
    private static final Logger log = LogManager.getLogger(GetMessageResponse.class.getName());

    @JsonProperty("message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object message;

    public JsonObject getMessage() {
        log.traceEntry();
        return log.traceExit(JsonObject.mapFrom(message));
    }
}
