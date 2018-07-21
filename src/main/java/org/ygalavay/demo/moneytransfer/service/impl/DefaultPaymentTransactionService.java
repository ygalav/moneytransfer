package org.ygalavay.demo.moneytransfer.service.impl;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;

public class DefaultPaymentTransactionService implements PaymentTransactionService {

    private Logger LOG = LoggerFactory.getLogger(DefaultPaymentTransactionService.class);

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MoneyLockRepository moneyLockRepository;
    private final JDBCClient jdbcClient;

    public DefaultPaymentTransactionService(JDBCClient jdbcClient, PaymentTransactionRepository paymentTransactionRepository, MoneyLockRepository moneyLockRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.jdbcClient = jdbcClient;
        this.moneyLockRepository = moneyLockRepository;
    }

    @Override
    public Single<PaymentTransaction> openPaymentTransaction(Account sender, Account recipient, Currency currency, Double amount) {
        PaymentTransaction paymentTransaction = new PaymentTransaction().setSender(sender).setRecipient(recipient);
        return jdbcClient.rxGetConnection()
            .flatMap(connection -> connection.rxSetAutoCommit(false)
            .andThen(paymentTransactionRepository.save(paymentTransaction, connection))
            .flatMap(savedTransaction -> {
                MoneyLock moneyLock = new MoneyLock()
                    .setAccount(sender)
                    .setAmount(amount)
                    .setPaymentTransaction(savedTransaction)
                    .setCurrency(currency);
                return moneyLockRepository
                    .save(moneyLock, connection)
                    .flatMap(lock -> connection
                        .rxCommit()
                        .andThen(Single.just(paymentTransaction.setMoneyLock(lock))));
            })
            .doOnError(error -> {
                LOG.error(
                    String.format("Error happened when saving transaction performing payment transaction, sender [%s], recipient [%s], currency [%s], amount [%s]",
                        sender.getEmail(), recipient.getEmail(), currency.name(), amount));
                connection.rxRollback();
            }));
    }

    @Override
    public Completable fulfillPaymentTransaction(String transactionId) {
        return jdbcClient
            .rxGetConnection().flatMap(connection -> connection.rxSetAutoCommit(false)
                .andThen(Single.create(subscriber -> {
                    paymentTransactionRepository.findById(transactionId)
                        .flatMap(paymentTransaction -> {
                            System.out.println("Transaction found");
                            return Single.just(paymentTransaction);
                        });
                }))).toCompletable();
    }
}
