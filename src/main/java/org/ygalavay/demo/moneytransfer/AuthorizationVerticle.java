package org.ygalavay.demo.moneytransfer;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.ygalavay.demo.moneytransfer.configuration.DependencyManager;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;
import org.ygalavay.demo.moneytransfer.model.AuthorizeResult;
import org.ygalavay.demo.moneytransfer.repository.TestDataCreator;

public class AuthorizationVerticle extends AbstractVerticle {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_HEADER_NAME = "content-type";

    private final Logger LOG = LoggerFactory.getLogger(AuthorizationVerticle.class);

    private TransferFacade transferFacade;
    private JDBCClient jdbc;

    @Override
    public void start(Future<Void> fut) throws Exception {
        LOG.info("Strating MoneyTransferVerticle");
        transferFacade = DependencyManager.getInstance(vertx, config()).getTransferFacade();

        jdbc = JDBCClient.createShared(vertx, config(), "MoneyTransfer-Collection");
        TestDataCreator.of(jdbc).createDatabaseStructure()
            .andThen(TestDataCreator.of(jdbc).createUserData())
            .doOnError(throwable -> fut.fail(throwable.getMessage()))
            .doOnComplete(() -> jdbc.close())
            .subscribe(() -> startWebApp(
                (http) -> {
                    completeStartup(http, fut);
                }
            ));

    }


    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        Router router = Router.router(vertx);
        router.route("/api/transactions*").handler(BodyHandler.create());
        router.post("/api/transactions").handler(context -> {
            TransferRequest transferRequest = Json.decodeValue(context.getBodyAsString(),
                TransferRequest.class);
            HttpServerResponse response = setContentType(context.response(), CONTENT_TYPE_APPLICATION_JSON);
            transferFacade.authorize(transferRequest)
                .doOnError(throwable -> {
                    response.setStatusCode(500);
                    response.end(Json.encodePrettily(TransferResponse.UNKNOWN_EROOR));
                })
                .subscribe(result -> {
                if (AuthorizeResult.ACCEPTED == result.getResult()) {
                    response.setStatusCode(201);
                }
                else {
                    response.setStatusCode(400);
                }
                response.end(Json.encodePrettily(result));
            });
        });

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080),
                next);

    }

    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
        if (http.succeeded()) {
            fut.complete();
        } else {
            fut.fail(http.cause());
        }
    }

    private HttpServerResponse setContentType(final HttpServerResponse response, final String contentType) {
        return response.putHeader(CONTENT_TYPE_HEADER_NAME, contentType);
    }
}
