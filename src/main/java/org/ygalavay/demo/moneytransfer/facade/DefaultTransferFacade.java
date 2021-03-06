package org.ygalavay.demo.moneytransfer.facade;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import org.ygalavay.demo.moneytransfer.configuration.Constants;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;

import java.math.BigDecimal;

import static org.ygalavay.demo.moneytransfer.configuration.Constants.EVENT_DO_CAPTURE;

public class DefaultTransferFacade implements TransferFacade {

    private Logger log = LoggerFactory.getLogger(DefaultTransferFacade.class);

    public DefaultTransferFacade(Vertx vertx, JsonObject config, AccountService accountService, PaymentTransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.vertx = vertx;
        this.config = config;
    }

    private final AccountService accountService;
    private final PaymentTransactionService transactionService;
    private final Vertx vertx;
    private final JsonObject config;


    @Override
    public Single<TransferResponse> authorize(TransferRequest transferRequest) {
        return accountService.getByEmail(transferRequest.getSender())
            .flatMap(sender -> {
                if (! sender.getCurrency().equals(transferRequest.getCurrency())) return Single.just(TransferResponse.CURRENCY_NOT_MATCH);

                BigDecimal availableBalance = accountService.getAvailableBalanceForAccount(sender);

                if (availableBalance.compareTo(BigDecimal.valueOf(transferRequest.getAmount())) < 0) return Single.just(TransferResponse.LOW_BALLANCE);
                else {
                    return accountService.getByEmail(transferRequest.getRecipient())
                        .flatMap(
                            recipient -> transactionService.openPaymentTransaction(sender, recipient, transferRequest.getCurrency(), transferRequest.getAmount()))
                        .doOnSuccess(transaction -> {
                            log.info(String.format("Publish successful payment transaction to fulfill, transaction id: [%s]", transaction.getId()));
                            vertx
                                .eventBus()
                                .publish(config.getString(EVENT_DO_CAPTURE), transaction.getId());
                        })
                        .flatMap(paymentTransaction -> Single.just(TransferResponse.CREATED.setTransactionId(paymentTransaction.getId())));
                }
            });
    }

    @Override
    public Completable fulfillTransaction(final String transactionId) {
        return transactionService
            .fulfillPaymentTransaction(transactionId)
            .doOnComplete( () -> {
                vertx.eventBus().publish(config.getString(Constants.EVENT_FULFILLMENT_SUCCESS), transactionId);
            });
    }
}
