package org.ygalavay.demo.moneytransfer;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.transfer.service.TransactionService;

import java.math.BigDecimal;

public class DefaultTransferFacade implements TransferFacade {

    public DefaultTransferFacade(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    private final AccountService accountService;
    private final TransactionService transactionService;


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
                        .flatMap(paymentTransaction -> Single.just(TransferResponse.CREATED));
                }
            });


    }
}
