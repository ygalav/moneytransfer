package org.ygalavay.demo.moneytransfer.facade;

import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.Vertx;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;

import java.math.BigDecimal;

public class DefaultTransferFacade implements TransferFacade {

    private Logger log = LoggerFactory.getLogger(DefaultTransferFacade.class);

    public DefaultTransferFacade(Vertx vertx, AccountService accountService, PaymentTransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.vertx = vertx;
    }

    private final AccountService accountService;
    private final PaymentTransactionService transactionService;
    private final Vertx vertx;


    @Override
    public Single<TransferResponse> authorize(TransferRequest transferRequest) {
        return accountService.getByEmail(transferRequest.getSender())
            .flatMap(sender -> {
                if (! sender.getCurrency().equals(transferRequest.getCurrency())) return Single.just(TransferResponse.CURRENCY_NOT_MATCH);
                if (BigDecimal.valueOf(sender.getBalance()).compareTo(BigDecimal.valueOf(transferRequest.getAmount())) < 0) return Single.just(TransferResponse.LOW_BALLANCE);
                else {
                    return accountService.getByEmail(transferRequest.getRecipient())
                        .flatMap(
                            recipient -> transactionService.openPaymentTransaction(sender, recipient, transferRequest.getCurrency(), transferRequest.getAmount()))
                        .doOnSuccess(transaction -> {
                            log.info(String.format("Publish successful payment transaction to fulfill, transaction id: [%s]", transaction.getId()));
                            vertx.eventBus().rxSend("transactionsToFulfill", transaction);
                        })
                        .flatMap(paymentTransaction -> Single.just(TransferResponse.CREATED));
                }
            });


    }
}
