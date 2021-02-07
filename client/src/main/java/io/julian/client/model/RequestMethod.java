package io.julian.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum RequestMethod {
    @JsonProperty("GET")
    GET,
    @JsonProperty("POST")
    POST,
    @JsonProperty("DELETE")
    DELETE,
}
