package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.model.PaymentTransactionStatus;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;

import java.util.Arrays;
import java.util.UUID;

public class DefaultPaymentTransactionRepository implements PaymentTransactionRepository {

    private static final String CREATE_PAYMENT_TRANSACTION_QUERY = "INSERT INTO payment_transactions (id, sender, recipient, status) values (?, ?, ?, ?)";
    private static final String UPDATE_PAYMENT_TRANSACTION_QUERY = "UPDATE payment_transactions SET sender=?, recipient=?, status=? WHERE id=?";
    private static final String FIND_PAYMENT_TRANSACTION_BY_ID_QUERY =
        "SELECT transaction.id, transaction.sender, transaction.recipient , " +
            "lock.id, lock.amount, lock.currency, " +
            "sender.email, sender.name, sender.surname, sender.balance, sender.currency, " +
            "recipient.email, recipient.name, recipient.surname, recipient.balance, recipient.currency, transaction.status " +
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
    public Single<PaymentTransaction> create(PaymentTransaction transaction) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(
                connection -> create(transaction, connection)
                .doFinally(connection::close)
            );
    }

    @Override
    public Single<PaymentTransaction> update(PaymentTransaction transaction) {
        return jdbcClient.rxGetConnection()
            .flatMap(connection ->
                update(transaction, connection)
                .doFinally(connection::close)
            );
    }

    @Override
        public Single<PaymentTransaction>update(PaymentTransaction transaction, SQLConnection connection) {
        final JsonArray params = new JsonArray(
            Arrays.asList(
                transaction.getSender().getEmail(),
                transaction.getRecipient().getEmail(),
                transaction.getStatus().name(),
                transaction.getId()
            ));
        return connection.rxUpdateWithParams(UPDATE_PAYMENT_TRANSACTION_QUERY, params)
            .flatMap(updateResult -> Single.just(transaction));
    }

    @Override
    public Single<PaymentTransaction> create(PaymentTransaction transaction, SQLConnection connection) {
        if (transaction.getId() == null) {
            transaction.setId(UUID.randomUUID().toString());
        }
        final JsonArray params = new JsonArray(
            Arrays.asList(
                transaction.getId(),
                transaction.getSender().getEmail(),
                transaction.getRecipient().getEmail(),
                transaction.getStatus().name()
            ));
        return connection.rxUpdateWithParams(CREATE_PAYMENT_TRANSACTION_QUERY, params)
            .flatMap(updateResult -> Single.just(transaction));
    }

    @Override
    public Single<PaymentTransaction> findById(String transactionId) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(connection -> connection
                .rxQuerySingleWithParams(FIND_PAYMENT_TRANSACTION_BY_ID_QUERY, new JsonArray().add(transactionId))
                .doFinally(connection::close)
                .flatMap(result -> {
                    PaymentTransaction paymentTransaction = new PaymentTransaction();
                    paymentTransaction.setId(result.getString(0));
                    paymentTransaction.setStatus(PaymentTransactionStatus.valueOf(result.getString(16)));

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
                    return Single.just(paymentTransaction);
                })
            );
    }
}
