package org.ygalavay.demo.moneytransfer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientResponse;
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
public class AuthorizationVerticleTestMultipleAuthorizations {

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
        vertx.deployVerticle(AuthorizationVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @Test
    public void shouldFailIfAccountTotalMinusLockedAmountsIsLessThanTransferAmount(TestContext context) {
        Async async = context.async(2);
        final TransferRequest request = new TransferRequest()
            .setSender("account1@mail.com")
            .setRecipient("ygalavay@mail.com")
            .setCurrency(Currency.USD)
            .setAmount(55.0d);

        requestAuthorization(request, response -> {
            context.assertEquals(response.statusCode(), 201);
            context.assertTrue(response.headers().get("content-type").contains("application/json"));
            response.bodyHandler(body -> {
                TransferResponse transferResponse = Json.decodeValue(body, TransferResponse.class);
                context.assertTrue(transferResponse.getResult() == AuthorizeResult.ACCEPTED);
                async.complete();

                requestAuthorization(request, res -> {
                    context.assertEquals(res.statusCode(), 400);
                    context.assertTrue(res.headers().get("content-type").contains("application/json"));
                    res.bodyHandler(b -> {
                        TransferResponse response1 = Json.decodeValue(b, TransferResponse.class);
                        context.assertEquals(AuthorizeResult.FAILED_LOW_BALANCE, response1.getResult());
                        async.complete();
                    });
                });

            });
        });
    }

    public void requestAuthorization(TransferRequest request, Handler<HttpClientResponse> responseHandler) {
        final String json = Json.encodePrettily(request);
        vertx.createHttpClient()
            .post(config.getInteger("http.port"), "localhost", "/api/transactions")
            .putHeader("content-type", "application/json")
            .putHeader("content-length", Integer.toString(json.length()))
            .handler(responseHandler)
            .write(json)
            .end();
    }





    @After
    public void tearDown(TestContext context) throws Exception {
        vertx.close(context.asyncAssertSuccess());
    }

}
