import api.Server;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import static api.Server.DEFAULT_HOST;
import static api.Server.DEFAULT_SERVER_PORT;

public class main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Server server = new Server();
        Promise<Boolean> hasDeployed = server.startServer(vertx);

        hasDeployed.future().onSuccess(res -> {
            HttpServer api = vertx.createHttpServer(new HttpServerOptions()
                    .setPort(DEFAULT_SERVER_PORT)
                    .setHost(DEFAULT_HOST));
            api.requestHandler(server.getRouterFactory().getRouter()).listen();
        });
    }
}
