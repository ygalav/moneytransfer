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
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.repository.TestDataCreator;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;

@RunWith(VertxUnitRunner.class)
public class CapturingVerticleTest {

    private Vertx vertx;
    private JsonObject config;
    private JDBCClient jdbcClient;
    private TransferFacade transferFacade;
    private DependencyManager dependencyManager;

    @Before
    public void setUp(TestContext context) throws Exception {

        vertx = Vertx.vertx();
        byte[] bytes = Files.readAllBytes(new File("src/test/resources/config.json").toPath());
        config = new JsonObject(new String(bytes, "UTF-8"));

        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config);
        jdbcClient = JDBCClient.createShared(vertx, config, "MoneyTransfer-Collection");
        dependencyManager = DependencyManager.getInstance(vertx, config);
        transferFacade = dependencyManager.getTransferFacade();

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

        vertx.eventBus()
            .consumer(config.getString(Constants.EVENT_DO_CAPTURE))
            .handler(message -> {
                asyncEventStartCaptureReceived.complete();
            });

        Async asyncEventFulfillmentSuccess = context.async();
        asyncEventFulfillmentSuccess.await(5000);
        vertx.eventBus()
            .consumer(config.getString(Constants.EVENT_FULFILLMENT_SUCCESS))
            .handler(message -> {
                asyncEventFulfillmentSuccess.complete();
            });
    }


    @Test
    public void shouldChargeMoneyFromSenderIfTransactionSuccess(TestContext context) {
        final String senderEmail = "account1@mail.com";
        final String recipientEmail = "ygalavay@mail.com";
        final double amountToCharge = 50.0;
        TransferRequest transferRequest = new TransferRequest()
            .setSender(senderEmail).setRecipient(recipientEmail).setAmount(amountToCharge).setCurrency(Currency.USD);

        final Account sender = dependencyManager.getAccountService().getByEmail(senderEmail).blockingGet();
        final Account recipient = dependencyManager.getAccountService().getByEmail(recipientEmail).blockingGet();

        final BigDecimal senderBalanceBefore = BigDecimal.valueOf(sender.getBalance());
        final BigDecimal recipientBalanceBefore = BigDecimal.valueOf(recipient.getBalance());

        transferFacade.authorize(transferRequest)
            .doOnError(error -> {
                context.fail();
            })
            .subscribe();

        Async asyncCheckBalance = context.async();
        asyncCheckBalance.await(5000);

        vertx.eventBus()
            .<String>consumer(config.getString(Constants.EVENT_FULFILLMENT_SUCCESS))
            .handler(message -> {
                String transactionId = message.body();
                dependencyManager.getPaymentTransactionRepository()
                    .findById(transactionId)
                    .subscribe(paymentTransaction -> {
                        BigDecimal newSenderBalance = BigDecimal.valueOf(paymentTransaction.getSender().getBalance());
                        BigDecimal newRecipientBalance = BigDecimal.valueOf(paymentTransaction.getRecipient().getBalance());
                        BigDecimal amount = BigDecimal.valueOf(amountToCharge);

                        context.assertTrue(newSenderBalance.equals(senderBalanceBefore.subtract(amount)));
                        context.assertTrue(newRecipientBalance.equals(recipientBalanceBefore.add(amount)));
                        asyncCheckBalance.complete();
                    });
            });
    }

}
