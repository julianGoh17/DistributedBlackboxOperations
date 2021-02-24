package io.julian.zookeeper.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

@Getter
public class CandidateInformation {
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String CANDIDATE_NUMBER_KEY = "candidate_number";

    private final String host;
    private final int port;
    private final long candidateNumber;

    @JsonCreator

    public CandidateInformation(@JsonProperty(HOST_KEY) final String host,
                                @JsonProperty(PORT_KEY) final int port,
                                @JsonProperty(CANDIDATE_NUMBER_KEY) final long candidateNumber) {
        this.host = host;
        this.port = port;
        this.candidateNumber = candidateNumber;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(HOST_KEY, host)
            .put(PORT_KEY, port)
            .put(CANDIDATE_NUMBER_KEY, candidateNumber);
    }
}
