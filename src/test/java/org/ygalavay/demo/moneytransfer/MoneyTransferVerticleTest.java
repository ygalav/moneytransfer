package org.ygalavay.demo.moneytransfer;

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
import org.ygalavay.demo.moneytransfer.configuration.Constants;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.model.AuthorizeResult;
import org.ygalavay.demo.moneytransfer.model.Currency;

import java.io.File;
import java.nio.file.Files;

@RunWith(VertxUnitRunner.class)
public class MoneyTransferVerticleTest {

    private Integer port;
    private Vertx vertx;
    private JsonObject config;

    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();

        byte[] bytes = Files.readAllBytes(new File("src/test/resources/config.json").toPath());
        config = new JsonObject(new String(bytes, "UTF-8"));
        port = config.getInteger("http.port");
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config);
        vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
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
            .post(config.getInteger("http.port"), "localhost", "/api/transactions")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 201);
                context.assertTrue(response.headers().get("content-type").contains("application/json"));
                response.bodyHandler(body -> {
                    TransferResponse transferResponse = Json.decodeValue(body, TransferResponse.class);
                    context.assertTrue(transferResponse.getResult() == AuthorizeResult.ACCEPTED);
                    async.complete();
                });
            })
            .write(json)
            .end();
    }


    @Test
    public void shouldStartFulfillingSuccessTransaction(TestContext context) {
        Async restResponseAsync = context.async();
        Async captureEventReceivedAcync = context.async();

        final TransferRequest request = new TransferRequest()
            .setSender("account1@mail.com")
            .setRecipient("ygalavay@mail.com")
            .setCurrency(Currency.USD)
            .setAmount(50.0d);
        final String json = Json.encodePrettily(request);

        vertx.createHttpClient()
            .post(port, "localhost", "/api/transactions")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 201);
                response.bodyHandler(body -> {
                    TransferResponse transferResponse = Json.decodeValue(body, TransferResponse.class);
                    context.assertTrue(transferResponse.getResult() == AuthorizeResult.ACCEPTED);
                    restResponseAsync.complete();
                });
            })
            .write(json)
            .end();
        vertx.eventBus().<String>consumer(config.getString(Constants.EVENT_DO_CAPTURE)).handler(objectMessage -> {
            String transactionId = objectMessage.body();
            context.assertTrue(transactionId != null);
            captureEventReceivedAcync.complete();
        });
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
            .post(port, "localhost", "/api/transactions")
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
