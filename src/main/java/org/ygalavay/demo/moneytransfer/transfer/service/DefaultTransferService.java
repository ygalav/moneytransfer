package org.ygalavay.demo.moneytransfer.transfer.service;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;

import java.math.BigDecimal;

public class DefaultTransferService implements TransferService {

    public DefaultTransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private AccountRepository accountRepository;

    @Override
    public Single<TransferResponse> authorize(TransferRequest transferRequest) {
        return accountRepository.getByEmail(transferRequest.getSender())
            .map(account -> {
                try {
                    if (! account.getCurrency().equals(transferRequest.getCurrency())) return TransferResponse.CURRENCY_NOT_MATCH;
                    if (BigDecimal.valueOf(account.getBalance()).compareTo(BigDecimal.valueOf(transferRequest.getAmount())) < 0) return TransferResponse.LOW_BALLANCE;
                    else return TransferResponse.CREATED;
                }

                catch (Exception e) {
                    return TransferResponse.UNKNOWN_EROOR;
                }
            });
    }
}
