package io.julian.server.models.coordination;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Getter
@Setter
public class CoordinationMessage {
    private static final Logger log = LogManager.getLogger(CoordinationMessage.class.getName());
    private final CoordinationMetadata metadata;
    private final JsonObject message;
    private final JsonObject definition;

    public static final String METADATA_KEY = "metadata";
    public static final String MESSAGE_KEY = "message";
    public static final String DEFINITION_KEY = "definition";

    public static final String DECODE_EXCEPTION_FORMAT_STRING = "Could not decode into CoordinationResponse because JSON is missing field '%s'";

    public CoordinationMessage(@JsonProperty(METADATA_KEY) final CoordinationMetadata metadata,
                               @JsonProperty(MESSAGE_KEY) final JsonObject message,
                               @JsonProperty(DEFINITION_KEY) final JsonObject definition) {
        this.metadata = metadata;
        this.message = message;
        this.definition = definition;
    }

    @JsonCreator
    public static CoordinationMessage fromJson(final JsonObject jsonObject) throws DecodeException  {
        log.traceEntry(() -> jsonObject);
        Optional<CoordinationMetadata> metadata = Optional.ofNullable(jsonObject.getJsonObject(METADATA_KEY))
            .map(metadataJson -> metadataJson.mapTo(CoordinationMetadata.class));
        if (metadata.isEmpty()) {
            throw new DecodeException(String.format(DECODE_EXCEPTION_FORMAT_STRING, METADATA_KEY));
        }

        Optional<JsonObject> message = Optional.ofNullable(jsonObject.getJsonObject(MESSAGE_KEY));
        if (message.isEmpty()) {
            throw new DecodeException(String.format(DECODE_EXCEPTION_FORMAT_STRING, MESSAGE_KEY));
        }
        Optional<JsonObject> userDefinition = Optional.ofNullable(jsonObject.getJsonObject(DEFINITION_KEY));
        if (userDefinition.isEmpty()) {
            throw new DecodeException(String.format(DECODE_EXCEPTION_FORMAT_STRING, DEFINITION_KEY));
        }
        return log.traceExit(new CoordinationMessage(metadata.get(), message.get(), userDefinition.get()));
    }

    @JsonValue
    public JsonObject toJson() {
        log.traceEntry();
        return log.traceExit(new JsonObject()
            .put(METADATA_KEY, metadata.toJson())
            .put(MESSAGE_KEY, message)
            .put(DEFINITION_KEY, definition));
    }
}
