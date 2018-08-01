package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultMoneyLockRepository implements MoneyLockRepository {

    private static final String CREATE_MONEY_LOCK_QUERY =
        "INSERT INTO money_locks (id, amount, currency, transaction, account) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_MONEY_LOCK_QUERY =
        "UPDATE money_locks SET amount =?, currency=?, transaction=?, account=? WHERE id=?";

    private final JDBCClient jdbcClient;

    public DefaultMoneyLockRepository(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Single<MoneyLock> create(MoneyLock moneyLock) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(connection -> create(moneyLock, connection)
                .doFinally(connection::close));
    }

    @Override
    public Single<MoneyLock> update(MoneyLock moneyLock) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(connection -> update(moneyLock, connection)
                .doFinally(connection::close));
    }

    @Override
    public Single<MoneyLock> update(MoneyLock moneyLock, SQLConnection connection) {
        JsonArray parameters = new JsonArray()
            .add(moneyLock.getAmount())
            .add(moneyLock.getCurrency().name())
            .add(moneyLock.getPaymentTransaction().getId())
            .add(moneyLock.getAccount().getEmail())
            .add(moneyLock.getId());
        return connection.rxUpdateWithParams(UPDATE_MONEY_LOCK_QUERY, parameters).flatMap(updateResult ->  Single.just(moneyLock));
    }

    @Override
    public Single<List<MoneyLock>> findActiveLocksForAccount(final Account account) {
        String query = "SELECT lock.id, lock.amount, lock.currency FROM money_locks as lock LEFT JOIN payment_transactions AS transaction ON lock.transaction=transaction.id " +
                "WHERE lock.account=? and transaction.status='CREATED'";
        return jdbcClient.rxQueryWithParams(query, new JsonArray().add(account.getEmail())).flatMap(resultSet -> {
            List<MoneyLock> locks = resultSet
                .getResults()
                .stream()
                .map(MoneyLock::of)
                .peek(lock -> lock.setAccount(account))
                .collect(Collectors.toList());
            return Single.just(locks);
        });
    }

    @Override
    public Single<MoneyLock> create(MoneyLock moneyLock, SQLConnection connection) {
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
