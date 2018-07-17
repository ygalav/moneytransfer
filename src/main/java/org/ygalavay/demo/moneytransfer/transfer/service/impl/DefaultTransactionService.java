package org.ygalavay.demo.moneytransfer.transfer.service.impl;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.transfer.service.TransactionService;

public class DefaultTransactionService implements TransactionService {


    @Override
    public Single<PaymentTransaction> openPaymentTransaction(Account sender, Account recipient, Currency currency, Double amount) {
        return Single.just(new PaymentTransaction());
    }
}
