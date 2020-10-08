package endpoints;

import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import models.ErrorResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ErrorHandler {
    private static final Logger logger = LogManager.getLogger(ErrorHandler.class.getName());
    public static final int ERROR_RESPONSE_CODE = 400;

    public void handle(RoutingContext context) {
        logger.traceEntry(() -> context);
        ErrorResponse error = new ErrorResponse(ERROR_RESPONSE_CODE, context.failure() != null ? context.failure() : new Exception("Not Found"));
        logger.error(error.getException());

        context.response()
            .setStatusCode(ERROR_RESPONSE_CODE)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(new ErrorResponse(ERROR_RESPONSE_CODE, context.failure() != null ? context.failure() : new Exception("Not Found")).toJson().encodePrettily());
        context.fail(error.getException());
        logger.traceExit();
    }
}
