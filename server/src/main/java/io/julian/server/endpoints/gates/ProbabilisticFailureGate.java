package io.julian.server.endpoints.gates;

import io.julian.server.components.Controller;
import io.julian.server.models.ServerStatus;
import io.julian.server.models.response.ErrorResponse;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ProbabilisticFailureGate implements HandlerGate {
    private static final Logger log = LogManager.getLogger(ProbabilisticFailureGate.class);
    public static final int FAILURE_STATUS_CODE = 500;
    public static final String SUCCESS_MESSAGE = String.format("Message has passed '%s' Gate", ProbabilisticFailureGate.class.getName());
    public static final String FAILURE_MESSAGE = String.format("Message has failed '%s' Gate", ProbabilisticFailureGate.class.getName());
    private final Random random = new Random();

    @Override
    public Future<String> handle(final RoutingContext context, final Controller controller) {
        log.traceEntry(() -> context, () -> controller);
        Promise<String> hasPassedGate = Promise.promise();
        if (ServerStatus.PROBABILISTIC_FAILURE.equals(controller.getStatus()) && random.nextFloat() <= controller.getFailureChance()) {
            context.response()
                .setStatusCode(FAILURE_STATUS_CODE)
                .end(new ErrorResponse(FAILURE_STATUS_CODE, new Exception(FAILURE_MESSAGE))
                    .toJson()
                    .encodePrettily());
            hasPassedGate.fail(FAILURE_MESSAGE);
        } else {
            log.trace(SUCCESS_MESSAGE);
            hasPassedGate.complete(SUCCESS_MESSAGE);
        }

        return log.traceExit(hasPassedGate.future());
    }
}
