package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;

import java.util.Arrays;
import java.util.UUID;

public class DefaultPaymentTransactionRepository implements PaymentTransactionRepository {

    private static final String CREATE_PAYMENT_TRANSACTION_QUERY = "INSERT INTO payment_transactions (id, sender, recipient) values (?, ?, ?)";

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
}
