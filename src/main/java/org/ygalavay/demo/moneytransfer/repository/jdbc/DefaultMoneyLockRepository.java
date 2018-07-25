package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;

import java.util.Arrays;
import java.util.UUID;

public class DefaultMoneyLockRepository implements MoneyLockRepository {

    private static final String CREATE_MONEY_LOCK_QUERY =
        "INSERT INTO money_locks (id, amount, currency, transaction, account) VALUES (?, ?, ?, ?, ?)";

    private final JDBCClient jdbcClient;

    public DefaultMoneyLockRepository(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Single<MoneyLock> save(MoneyLock moneyLock) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(connection -> save(moneyLock, connection)
                .doFinally(connection::close));
    }

    @Override
    public Single<MoneyLock> save(MoneyLock moneyLock, SQLConnection connection) {
        if (moneyLock.getId() == null) {
            moneyLock.setId(UUID.randomUUID().toString());
        }
        JsonArray params = new JsonArray(
            Arrays.asList(
                moneyLock.getId(), moneyLock.getAmount(), moneyLock.getCurrency().name(), moneyLock.getPaymentTransaction().getId(), moneyLock.getAccount().getEmail()
            ));
        return connection
            .rxUpdateWithParams(CREATE_MONEY_LOCK_QUERY, params)
            .flatMap(updateResult -> Single.just(moneyLock));
    }
}
