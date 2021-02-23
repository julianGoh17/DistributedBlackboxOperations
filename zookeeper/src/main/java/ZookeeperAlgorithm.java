import io.julian.server.api.DistributedAlgorithm;
import io.julian.server.components.Controller;
import io.julian.server.components.MessageStore;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZookeeperAlgorithm extends DistributedAlgorithm {
    private final Logger logger = LogManager.getLogger(ZookeeperAlgorithm.class);

    public ZookeeperAlgorithm(final Controller controller, final MessageStore messageStore, final Vertx vertx) {
        super(controller, messageStore, vertx);
    }

    @Override
    public void actOnCoordinateMessage() {
        logger.traceEntry();
        logger.traceExit();
    }

    @Override
    public void actOnInitialMessage() {
        logger.traceEntry();
        logger.traceExit();
    }
}
