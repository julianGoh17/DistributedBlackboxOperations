package io.julian.server.models.response;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private final int statusCode;
    private final Throwable exception;

    public static final String STATUS_CODE_KEY = "statusCode";
    public static final String ERROR_KEY = "error";

    public ErrorResponse(final int statusCode, final Throwable exception) {
        this.statusCode = statusCode;
        this.exception = exception;
    }

    public JsonObject toJson() {
        return new JsonObject()
            .put(STATUS_CODE_KEY, this.statusCode)
            .put(ERROR_KEY, this.exception.getMessage());
    }

    public static ErrorResponse fromJson(final JsonObject jsonObject) {
        return new ErrorResponse(jsonObject.getInteger(STATUS_CODE_KEY),
            new Exception(jsonObject.getString(ERROR_KEY)));
    }
}
