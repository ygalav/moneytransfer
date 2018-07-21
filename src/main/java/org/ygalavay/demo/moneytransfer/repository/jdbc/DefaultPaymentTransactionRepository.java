package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;

import java.util.Arrays;
import java.util.UUID;

public class DefaultPaymentTransactionRepository implements PaymentTransactionRepository {

    private static final String CREATE_PAYMENT_TRANSACTION_QUERY = "INSERT INTO payment_transactions (id, sender, recipient) values (?, ?, ?)";
    private static final String FIND_PAYMENT_TRANSACTION_BY_ID_QUERY =
        "SELECT transaction.id, transaction.sender, transaction.recipient , " +
            "lock.id, lock.amount, lock.amount, lock.currency, " +
            "sender.email, sender.name, sender.surname, sender.balance, sender.currency, " +
            "recipient.email, recipient.name, recipient.surname, recipient.balance, recipient.currency " +
        "FROM payment_transactions AS transaction " +
            "LEFT JOIN money_locks AS lock ON lock.transaction = transaction.id " +
            "LEFT JOIN accounts AS sender ON transaction.sender = sender.email " +
            "LEFT JOIN accounts AS recipient ON transaction.recipient = recipient.email " +
        "WHERE transaction.id=?";

    private final JDBCClient jdbcClient;

    public DefaultPaymentTransactionRepository(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Single<PaymentTransaction> save(PaymentTransaction transaction) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(connection -> save(transaction, connection));
    }

    @Override
    public Single<PaymentTransaction> save(PaymentTransaction transaction, SQLConnection connection) {
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID().toString());
        }
        final JsonArray params = new JsonArray(
            Arrays.asList(
                transaction.getId(),
                transaction.getSender().getEmail(),
                transaction.getRecipient().getEmail()
            ));
        return connection.rxUpdateWithParams(CREATE_PAYMENT_TRANSACTION_QUERY, params)
            .flatMap(updateResult -> Single.just(transaction));
    }

    @Override
    public Single<PaymentTransaction> findById(String transactionId) {
        jdbcClient
            .rxGetConnection()
            .flatMap(connection -> connection
                .rxQuerySingleWithParams(FIND_PAYMENT_TRANSACTION_BY_ID_QUERY, new JsonArray().add(transactionId))
                .flatMap(result -> {
                    PaymentTransaction paymentTransaction = new PaymentTransaction();
                    paymentTransaction.setId(result.getString(0));

                    if (result.getString(3) == null) {
                        return Single.error(new IllegalStateException(String.format("There's no lock for payment transaction [%s]", transactionId)));
                    }

                    MoneyLock lock = new MoneyLock()
                        .setId(result.getString(3))
                        .setAmount(result.getDouble(4))
                        .setCurrency(Currency.valueOf(result.getString(5)))
                        .setPaymentTransaction(paymentTransaction);
                    paymentTransaction.setMoneyLock(lock);

                   Account sender = new Account()
                       .setEmail(result.getString(6))
                       .setName(result.getString(7))
                       .setSurname(result.getString(8))
                       .setBalance(result.getDouble(9))
                       .setCurrency(Currency.valueOf(result.getString(10)));

                    Account recipient = new Account()
                        .setEmail(result.getString(11))
                        .setName(result.getString(12))
                        .setSurname(result.getString(13))
                        .setBalance(result.getDouble(14))
                        .setCurrency(Currency.valueOf(result.getString(15)));

                    paymentTransaction.setSender(sender).setRecipient(recipient);

                        /*String a = "SELECT transaction.id, transaction.sender, transaction.recipient , \" +\n" + //3
                            "            \"lock.id, lock.amount lock.currency, \" +\n" + //3
                            "            \"sender.email, sender.name, sender.surname, sender.balance, sender.currency, \" +\n" + //5
                            "            \"recipient.email, recipient.name, recipient.surname, recipient.balance, recipient.currency \""; //5*/
                    return Single.just(paymentTransaction);
                })
            );
        return null;
    }
}
