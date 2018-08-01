package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultAccountRepository;

public class  TestDataCreator {

    private final JDBCClient jdbcClient;
    private TestDataCreator(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public static TestDataCreator of(JDBCClient jdbcClient) {
        return new TestDataCreator(jdbcClient);
    }

    public Completable createUserData() {
        final DefaultAccountRepository accountRepository = DefaultAccountRepository
            .of(jdbcClient, null);
        Account account = Account.of("account1@mail.com", "John", "Doe", 100d, Currency.USD);
        return accountRepository
            .create(account)
            .flatMap(
                (result) ->
                    accountRepository.create(Account.of("ygalavay@mail.com", "Yuriy", "Galavay", 100d, Currency.USD))
            )
            .flatMap(
                (result) -> accountRepository.create(Account.of("tgalavay@mail.com", "Tetyana", "Galavay", 100d, Currency.EUR))
            )
            .toCompletable();
    }

    public Completable createDatabaseStructure() {
        return jdbcClient.rxGetConnection()
            .flatMap(connection ->
                connection.rxUpdate("DROP ALL OBJECTS")
                .flatMap(result -> connection.rxUpdate(
                    "CREATE TABLE accounts(email VARCHAR(256) NOT NULL PRIMARY KEY, name VARCHAR(256), surname VARCHAR(256), balance DOUBLE, currency VARCHAR(3))")
                )
                .flatMap(result -> connection.rxUpdate(
                    "CREATE TABLE payment_transactions(id VARCHAR(256) NOT NULL PRIMARY KEY, sender VARCHAR(256), recipient VARCHAR(256), status VARCHAR(256))")
                )
                .flatMap(result -> connection.rxUpdate("CREATE TABLE money_locks " +
                    "(id VARCHAR(256) NOT NULL PRIMARY KEY, amount DOUBLE, currency VARCHAR(3), transaction VARCHAR(256), account VARCHAR(256)) "
                ))
                .flatMap(
                    result -> connection.rxUpdate("ALTER TABLE payment_transactions ADD CONSTRAINT fk1 FOREIGN KEY (sender) REFERENCES accounts(email)")
                )
                .flatMap(
                    result -> connection.rxUpdate("ALTER TABLE money_locks ADD CONSTRAINT fk2 FOREIGN KEY (transaction) REFERENCES payment_transactions(id)")
                )
                .flatMap(result -> Single.just(connection))
                .doFinally(() -> connection.close())
            )
            .toCompletable();
    }


}
