package endpoints;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import models.ErrorResponse;

public class ErrorHandler {
    public static final int ERROR_RESPONSE_CODE = 404;

    public void handle(RoutingContext context) {
        context.response()
                .setStatusCode(ERROR_RESPONSE_CODE)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end(new ErrorResponse(ERROR_RESPONSE_CODE, context.failure() != null ? context.failure() : new Exception("Not Found")).toJson().encodePrettily());
    }
}
