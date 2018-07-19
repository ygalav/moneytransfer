package org.ygalavay.demo.moneytransfer.transfer;


import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.web.Router;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.jdbc.DefaultAccountRepository;

import java.util.stream.Collectors;

public class TestDBDataVerticle extends AbstractVerticle {

    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_HEADER_NAME = "content-type";

    private JDBCClient jdbcClient;

    @Override
    public void start(Future<Void> fut) throws Exception {
        Router router = Router.router(vertx);
        jdbcClient = JDBCClient.createShared(vertx, config(), "MoneyTransfer-Collection");

        router.route("/test/transactions").handler(routingContext -> {
            HttpServerResponse response = setContentType(routingContext.response(), CONTENT_TYPE_APPLICATION_JSON);
            AccountRepository accountRepository = new DefaultAccountRepository(jdbcClient);
            jdbcClient.rxGetConnection() //Async get DB connection
                .flatMap(connection -> connection //When connection arrived
                    .rxQuery("SELECT id, sender, recipient FROM payment_transactions") //Run Query
                    .map(resultSet -> resultSet.getResults() //When result returned
                        .stream()
                        .map(jsonArray -> {
                            PaymentTransaction paymentTransaction = new PaymentTransaction();
                            paymentTransaction.setId(jsonArray.getString(0));
                            return accountRepository.getByEmail(jsonArray.getString(1)) //Get user from DB
                                .doOnSuccess(paymentTransaction::setSender) //When User comes
                                .toCompletable()
                                .andThen(
                                    accountRepository.getByEmail(jsonArray.getString(2)) //Get another user from DB
                                )
                                .doOnSuccess(paymentTransaction::setRecipient) //When another user comes
                                .to(acc -> paymentTransaction); //Transform this to Transaction
                        })
                        .collect(Collectors.toList()))) //Collect All results
                .doOnSuccess(paymentTransactions -> { //When evetything finishes
                    response.end(Json.encodePrettily(paymentTransactions)); //Rend user request
                });
        });

        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(8000, result -> {
                if (result.succeeded()) {
                    fut.complete();
                } else {
                    fut.fail(result.cause());
                }
            }
        );
    }

    private HttpServerResponse setContentType(final HttpServerResponse response, final String contentType) {
        return response.putHeader(CONTENT_TYPE_HEADER_NAME, contentType);
    }

}
