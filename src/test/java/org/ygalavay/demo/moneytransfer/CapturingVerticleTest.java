package org.ygalavay.demo.moneytransfer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ygalavay.demo.moneytransfer.configuration.Constants;
import org.ygalavay.demo.moneytransfer.configuration.DependencyManager;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.repository.TestDataCreator;

import java.io.File;
import java.nio.file.Files;

@RunWith(VertxUnitRunner.class)
public class CapturingVerticleTest {

    private Vertx vertx;
    private JsonObject config;
    private JDBCClient jdbcClient;
    private TransferFacade transferFacade;

    @Before
    public void setUp(TestContext context) throws Exception {
        vertx = Vertx.vertx();
        byte[] bytes = Files.readAllBytes(new File("src/test/resources/config.json").toPath());
        config = new JsonObject(new String(bytes, "UTF-8"));

        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config);
        jdbcClient = JDBCClient.createShared(vertx, config, "MoneyTransfer-Collection");
        transferFacade = DependencyManager.createTransferService(vertx, config);

        Async asyncInsertData = context.async();
        TestDataCreator.of(jdbcClient).createDatabaseStructure()
            .andThen(TestDataCreator.of(jdbcClient).createUserData())
            .subscribe(() -> {
                vertx.deployVerticle(CapturingVerticle.class.getName(), options);
                asyncInsertData.complete();
            });


    }

    @Test
    public void shouldSuccessfullyFulfillExistingTransaction(TestContext context) {
        TransferRequest transferRequest = new TransferRequest()
            .setSender("account1@mail.com").setRecipient("ygalavay@mail.com").setAmount(50.0).setCurrency(Currency.USD);
        transferFacade.authorize(transferRequest)
            .doOnError(error -> {
                context.fail();
            })
            .subscribe();

        Async asyncEventStartCaptureReceived = context.async();
        asyncEventStartCaptureReceived.await(3000);
        vertx.eventBus()
            .consumer(config.getString(Constants.EVENT_DO_CAPTURE))
            .handler(message -> {
                asyncEventStartCaptureReceived.complete();
            });

        Async asyncEventFulfillmentSuccess = context.async();
        vertx.eventBus()
            .consumer(config.getString(Constants.EVENT_FULFILLMENT_SUCCESS))
            .handler(message -> {
                asyncEventFulfillmentSuccess.complete();
            });
    }

}
