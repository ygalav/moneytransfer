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
import org.ygalavay.demo.moneytransfer.model.PaymentTransactionStatus;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;

import java.math.BigDecimal;

public class DefaultPaymentTransactionService implements PaymentTransactionService {

    private Logger LOG = LoggerFactory.getLogger(DefaultPaymentTransactionService.class);

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final MoneyLockRepository moneyLockRepository;
    private final AccountRepository accountRepository;
    private final JDBCClient jdbcClient;

    public DefaultPaymentTransactionService(JDBCClient jdbcClient, PaymentTransactionRepository paymentTransactionRepository, MoneyLockRepository moneyLockRepository, AccountRepository accountRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.jdbcClient = jdbcClient;
        this.moneyLockRepository = moneyLockRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Single<PaymentTransaction> openPaymentTransaction(Account sender, Account recipient, Currency currency, Double amount) {
        PaymentTransaction paymentTransaction = PaymentTransaction.open(sender, recipient);
        return jdbcClient.rxGetConnection()
            .flatMap(connection -> connection.rxSetAutoCommit(false)
            .andThen(paymentTransactionRepository.create(paymentTransaction, connection))
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

        return paymentTransactionRepository.findById(transactionId)
            .flatMap(paymentTransaction -> {
                Account sender = paymentTransaction.getSender();
                Account recipient = paymentTransaction.getRecipient();
                double amount = paymentTransaction.getMoneyLock().getAmount();
                chargeMoney(sender, recipient, amount); //To reduce transaction scope, we extract calculations outside

                return jdbcClient.rxGetConnection()
                    .flatMap(connection -> connection
                        .rxSetAutoCommit(false)
                        .andThen(accountRepository.update(sender)
                            .flatMap(result -> accountRepository.update(recipient))
                            .flatMap(result -> paymentTransactionRepository.update(paymentTransaction.setStatus(PaymentTransactionStatus.FINISHED)))
                            .doOnError(error -> {
                                connection.rxRollback();
                                paymentTransactionRepository
                                    .update(paymentTransaction.setStatus(PaymentTransactionStatus.FAILED))
                                    .subscribe();
                            })
                            .doOnSuccess(updateResult -> connection.rxCommit()))
                            .doFinally(connection::close));
            }).toCompletable();

    }

    private void chargeMoney(Account sender, Account recipient, double amount) {
        BigDecimal senderBalance = BigDecimal.valueOf(sender.getBalance());
        BigDecimal recipientBalance = BigDecimal.valueOf(sender.getBalance());
        BigDecimal amountToCharge = BigDecimal.valueOf(amount);

        sender.setBalance(senderBalance.subtract(amountToCharge).doubleValue());
        recipient.setBalance(recipientBalance.add(amountToCharge).doubleValue());
    }
}
