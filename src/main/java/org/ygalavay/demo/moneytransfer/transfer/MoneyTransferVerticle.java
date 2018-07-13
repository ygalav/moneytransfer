package org.ygalavay.demo.moneytransfer.transfer;

import io.vertx.core.Future;
import io.vertx.ext.web.handler.BodyHandler;
import org.ygalavay.demo.moneytransfer.transfer.configuration.DependencyManager;
import org.ygalavay.demo.moneytransfer.transfer.model.AuthorizeResult;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.service.TransferService;

public class MoneyTransferVerticle extends AbstractVerticle {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_HEADER_NAME = "content-type";

    private TransferService transferService;

    @Override
    public void start(Future<Void> fut) throws Exception {
        transferService = DependencyManager.createTransferService(vertx);
        Router router = Router.router(vertx);
        router.route("/api/transactions*").handler(BodyHandler.create());
        router.post("/api/transactions").handler(context -> {
            TransferRequest transferRequest = Json.decodeValue(context.getBodyAsString(),
                TransferRequest.class);
            transferService.transfer(transferRequest).subscribe(result -> {
                HttpServerResponse response = setContentType(context.response(), CONTENT_TYPE_APPLICATION_JSON);
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
            .listen(
                config().getInteger("transaction.http.port", 8080),
                result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });

    }

    private HttpServerResponse setContentType(final HttpServerResponse response, final String contentType) {
        return response.putHeader(CONTENT_TYPE_HEADER_NAME, contentType);
    }
}
