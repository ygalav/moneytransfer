package org.ygalavay.demo.moneytransfer;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ygalavay.demo.moneytransfer.configuration.Constants;
import org.ygalavay.demo.moneytransfer.configuration.DependencyManager;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.AuthorizeResult;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.model.PaymentTransactionStatus;
import org.ygalavay.demo.moneytransfer.repository.TestDataCreator;
import org.ygalavay.demo.moneytransfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;

import static org.ygalavay.demo.moneytransfer.configuration.Constants.EVENT_DO_CAPTURE;

@RunWith(VertxUnitRunner.class)
public class CapturingVerticleTest {

    private static final Logger LOG = LoggerFactory.getLogger(CapturingVerticleTest.class);

    private static Vertx vertx;
    private static JsonObject config;
    private static JDBCClient jdbcClient;
    private TransferFacade transferFacade;
    private PaymentTransactionService transactionService;
    private AccountService accountService;
    private DependencyManager dependencyManager;

    @BeforeClass
    public static void beforeClass(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        byte[] bytes = Files.readAllBytes(new File("src/test/resources/config.json").toPath());
        config = new JsonObject(new String(bytes, "UTF-8"));

        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config);

        jdbcClient = JDBCClient.createNonShared(vertx, config);
        Async asyncInsertData = context.async();
        TestDataCreator.of(jdbcClient).createDatabaseStructure()
            .andThen(TestDataCreator.of(jdbcClient).createUserData())
            .subscribe(() -> {
                vertx.deployVerticle(CapturingVerticle.class.getName(), options, asyncResult -> {
                    if (asyncResult.failed()) {
                        context.fail();
                    }
                    asyncInsertData.complete();
                });

            });
    }

    @Before
    public void setUp(TestContext context) throws Exception {

        dependencyManager = DependencyManager.getInstance(vertx, config);
        transferFacade = dependencyManager.getTransferFacade();
        transactionService = dependencyManager.getTransactionService();
        accountService = dependencyManager.getAccountService();

    }

    @Test
    public void shouldChargeMoneyFromSenderIfTransactionSuccess(TestContext context) {
        final String senderEmail = "ygalavay@mail.com";
        final String recipientEmail = "account1@mail.com";
        final double amountToCharge = 50.0;
        TransferRequest transferRequest = new TransferRequest()
            .setSender(senderEmail).setRecipient(recipientEmail).setAmount(amountToCharge).setCurrency(Currency.USD);

        LOG.info("Running test shouldChargeMoneyFromSenderIfTransactionSuccess");
        final Account sender = dependencyManager.getAccountService().getByEmail(senderEmail).blockingGet();
        final Account recipient = dependencyManager.getAccountService().getByEmail(recipientEmail).blockingGet();

        final BigDecimal senderBalanceBefore = BigDecimal.valueOf(sender.getBalance());
        final BigDecimal recipientBalanceBefore = BigDecimal.valueOf(recipient.getBalance());

        Async asyncCheckBalance = context.async(1);

        final MessageConsumer<String> consumer = vertx.eventBus()
            .<String>consumer(config.getString(Constants.EVENT_FULFILLMENT_SUCCESS));
        consumer
            .handler(message -> {
                String transactionId = message.body();
                dependencyManager.getPaymentTransactionRepository()
                    .findById(transactionId)
                    .subscribe(paymentTransaction -> {
                        context.assertEquals(PaymentTransactionStatus.FINISHED, paymentTransaction.getStatus());
                        BigDecimal newSenderBalance = BigDecimal.valueOf(paymentTransaction.getSender().getBalance());
                        BigDecimal newRecipientBalance = BigDecimal.valueOf(paymentTransaction.getRecipient().getBalance());
                        BigDecimal amount = BigDecimal.valueOf(amountToCharge);

                        context.assertTrue(newSenderBalance.equals(senderBalanceBefore.subtract(amount)));
                        context.assertTrue(newRecipientBalance.equals(recipientBalanceBefore.add(amount)));
                        asyncCheckBalance.complete();
                        consumer.unregister();
                    });
            });

        transferFacade.authorize(transferRequest)
            .doOnError(error -> context.fail())
            .subscribe();
    }

    @Test
    public void shouldSendFailedMessageIfTransactionAmountIsNotCorrect(TestContext context) {
        LOG.info("Running test shouldSendFailedMessageIfTransactionAmountIsNotCorrect");

        //vertx.rxUndeploy(CapturingVerticle.class.getName()).subscribe();

        final String senderEmail = "ygalavay@mail.com";
        final String recipientEmail = "account1@mail.com";
        final double amountToCharge = 50.0;
        TransferRequest transferRequest = new TransferRequest()
            .setSender(senderEmail).setRecipient(recipientEmail).setAmount(amountToCharge).setCurrency(Currency.USD);

        Async asyncCheckBalance = context.async();

        final MessageConsumer<String> consumer = vertx.eventBus()
            .consumer(config.getString(Constants.EVENT_FULFILLMENT_UNKNOWN_ERROR));
        consumer
            .handler(message -> {
                String transactionId = message.body();
                dependencyManager.getPaymentTransactionRepository()
                    .findById(transactionId).subscribe(transaction -> {
                        context.assertEquals(PaymentTransactionStatus.FAILED, transaction.getStatus());
                        asyncCheckBalance.complete();
                    });

            });

        Account sender = accountService.getByEmail(senderEmail).blockingGet();
        Account recipient = accountService.getByEmail(recipientEmail).blockingGet();
        PaymentTransaction transaction = transactionService.openPaymentTransaction(sender, recipient, Currency.USD, amountToCharge).blockingGet();

        double notValidBalance = transaction.getSender().getBalance() + 10.0;
        jdbcClient.rxQueryWithParams("UPDATE money_locks SET amount=? WHERE id=?",
            new JsonArray(
                Arrays.asList(notValidBalance, transaction.getMoneyLock().getId())
            ))
            .blockingGet();
        vertx.eventBus().publish(config.getString(EVENT_DO_CAPTURE), transaction.getId());
    }

}
