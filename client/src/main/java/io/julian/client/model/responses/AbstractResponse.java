package io.julian.client.model.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
public abstract class AbstractResponse {
    private static final Logger log = LogManager.getLogger(AbstractResponse.class.getName());
    @JsonProperty("statusCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected int statusCode;

    @JsonProperty("error")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String error;

    public boolean isError() {
        log.traceEntry();
        return log.traceExit(error != null);
    }
}
