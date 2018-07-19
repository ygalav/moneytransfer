package org.ygalavay.demo.moneytransfer.transfer.service.impl;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLClient;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;
import org.ygalavay.demo.moneytransfer.transfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.transfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.PaymentTransactionService;

public class DefaultPaymentTransactionService implements PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MoneyLockRepository moneyLockRepository;
    private final SQLClient sqlClient;

    public DefaultPaymentTransactionService(SQLClient sqlClient, PaymentTransactionRepository paymentTransactionRepository, MoneyLockRepository moneyLockRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.sqlClient = sqlClient;
        this.moneyLockRepository = moneyLockRepository;
    }

    @Override
    public Single<PaymentTransaction> openPaymentTransaction(Account sender, Account recipient, Currency currency, Double amount) {
        PaymentTransaction paymentTransaction = new PaymentTransaction().setSender(sender).setRecipient(recipient);
        return sqlClient.rxGetConnection()
            .flatMap(connection -> connection.rxSetAutoCommit(false)
            .andThen(paymentTransactionRepository.save(paymentTransaction, connection))
            .flatMap(savedTransaction -> {
                MoneyLock moneyLock = new MoneyLock()
                    .setAmount(amount)
                    .setPaymentTransaction(savedTransaction)
                    .setCurrency(currency);
                return moneyLockRepository
                    .save(moneyLock, connection)
                    .flatMap(lock -> connection
                        .rxCommit()
                        .andThen(Single.just(paymentTransaction.setMoneyLock(lock))));
            }));
    }
}
