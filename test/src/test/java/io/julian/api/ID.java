package io.julian.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ID {
    private final static String FIRST_NAME_KEY = "first_name";
    private final static String LAST_NAME_KEY = "last_name";
    private final String firstName;
    private final String lastName;

    public static final ID EXAMPLE = new ID("test", "boy");

    public ID(@JsonProperty(FIRST_NAME_KEY) final String firstName,
              @JsonProperty(LAST_NAME_KEY) final String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(FIRST_NAME_KEY, firstName)
            .put(LAST_NAME_KEY, lastName);
    }
}
