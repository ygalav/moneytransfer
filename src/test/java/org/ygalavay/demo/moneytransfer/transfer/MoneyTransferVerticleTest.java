package org.ygalavay.demo.moneytransfer.transfer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.model.AuthorizeResult;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;

@RunWith(VertxUnitRunner.class)
public class MoneyTransferVerticleTest {

    public static final int PORT = 8080;
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", PORT)
            );
        vertx.deployVerticle(MoneyTransferVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @Test
    public void shouldAcceptTransferIfAccountHasProperBalanceAnCurrencyMatches(TestContext context) {
        Async async = context.async();
        final TransferRequest request = new TransferRequest()
            .setSender("account1@mail.com")
            .setRecipient("ygalavay@mail.com")
            .setCurrency(Currency.USD)
            .setAmount(50.0d);
        final String json = Json.encodePrettily(request);

        vertx.createHttpClient()
            .post(PORT, "localhost", "/api/transactions")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 201);
                context.assertTrue(response.headers().get("content-type").contains("application/json"));
                response.bodyHandler(body -> {
                    async.complete();
                });
            })
            .write(json)
            .end();
    }

    @Test
    public void shouldFailTransferIfAccountCurrencyIsDifferent(TestContext context) {
        Async async = context.async();
        final TransferRequest request = new TransferRequest()
            .setSender("account1@mail.com")
            .setRecipient("ygalavay@mail.com")
            .setCurrency(Currency.EUR)
            .setAmount(50.0d);
        final String json = Json.encodePrettily(request);

        vertx.createHttpClient()
            .post(PORT, "localhost", "/api/transactions")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 400);
                context.assertTrue(response.headers().get("content-type").contains("application/json"));
                response.bodyHandler(body -> {
                    TransferResponse transferResponse = Json.decodeValue(body, TransferResponse.class);
                    context.assertTrue(AuthorizeResult.FAILED_CURRENCY_NOT_MATCH == transferResponse.getResult());
                    async.complete();
                }).exceptionHandler(throwable -> {
                    context.fail(throwable);
                    async.complete();
                });
            })
            .write(json)
            .end();
    }


    @After
    public void tearDown(TestContext context) throws Exception {
        vertx.close(context.asyncAssertSuccess());
    }

}
