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
                    .create(moneyLock, connection)
                    .flatMap(lock -> connection
                        .rxCommit()
                        .doOnComplete(() -> LOG.info("Transaction [%s] has ben processed with status [%s]".format(paymentTransaction.getId(), paymentTransaction.getStatus())))
                        .andThen(Single.just(paymentTransaction.setMoneyLock(lock))));
            })
            .doOnError(error -> {
                LOG.error(
                    String.format("Error happened when saving transaction performing payment transaction, sender [%s], recipient [%s], currency [%s], amount [%s]",
                        sender.getEmail(), recipient.getEmail(), currency.name(), amount));
                connection.rxRollback();
            })
            .doFinally(connection::close));
    }

    @Override
    public Completable fulfillPaymentTransaction(String transactionId) {

        return paymentTransactionRepository.findById(transactionId)
            .flatMap(paymentTransaction -> {
                LOG.info(String.format("Changing sender, recipients ballance for transaction [%s]", transactionId));
                Account sender = paymentTransaction.getSender();
                Account recipient = paymentTransaction.getRecipient();
                double amount = paymentTransaction.getMoneyLock().getAmount();


                BigDecimal senderBalance = BigDecimal.valueOf(sender.getBalance());
                BigDecimal recipientBalance = BigDecimal.valueOf(recipient.getBalance());
                BigDecimal amountToCharge = BigDecimal.valueOf(amount);


                final BigDecimal newSenderBalance = senderBalance.subtract(amountToCharge);

                if (newSenderBalance.compareTo(BigDecimal.ZERO) < 0) {
                    return paymentTransactionRepository
                        .update(paymentTransaction.setStatus(PaymentTransactionStatus.FAILED))
                        .flatMap(transaction -> Single.error(new IllegalStateException(String.format("Not enough balance on sender's account to process transaction, id: %s", transaction.getId()))));
                }

                final BigDecimal newRecipientBalance = recipientBalance.add(amountToCharge);
                sender.setBalance(newSenderBalance.doubleValue());
                recipient.setBalance(newRecipientBalance.doubleValue());


                return jdbcClient.rxGetConnection()
                    .flatMap(connection -> connection
                        .rxSetAutoCommit(false)
                        .andThen(accountRepository.update(sender)
                            .flatMap(result -> accountRepository.update(recipient))
                            .flatMap(result ->
                                paymentTransactionRepository.update(paymentTransaction.setStatus(PaymentTransactionStatus.FINISHED), connection))
                            .doOnError(error -> connection.rxRollback().subscribe(() -> {
                                LOG.error(String.format("Error during fulfilling payment transaction [%s], rolling back tre transaction", transactionId), error);
                                connection.close();
                            }))
                            .doOnSuccess(updateResult -> {
                                connection.rxCommit().subscribe(() -> {
                                    connection.close();
                                    LOG.info(String.format("Transaction [%s] has been fulfilled successfully.", transactionId));
                                });
                            }))
                        .doFinally(() -> connection.close()));

            }).toCompletable();

    }

    private Completable chargeMoney(PaymentTransaction transaction, double amount) {
        Account sender = transaction.getSender();
        Account recipient = transaction.getRecipient();
        BigDecimal senderBalance = BigDecimal.valueOf(sender.getBalance());
        BigDecimal recipientBalance = BigDecimal.valueOf(recipient.getBalance());
        BigDecimal amountToCharge = BigDecimal.valueOf(amount);


        final BigDecimal newSenderBalance = senderBalance.subtract(amountToCharge);

        if (newSenderBalance.compareTo(BigDecimal.ZERO) < 0) {
            return Completable.error(
                new IllegalStateException(String.format("Not enough balance on sender's account to process transaction, id: %s", transaction.getId())));
        }

        final BigDecimal newRecipientBalance = recipientBalance.add(amountToCharge);
        sender.setBalance(newSenderBalance.doubleValue());
        recipient.setBalance(newRecipientBalance.doubleValue());
        return Completable.complete();
    }
}
