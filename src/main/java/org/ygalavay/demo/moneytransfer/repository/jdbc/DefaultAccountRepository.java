package org.ygalavay.demo.moneytransfer.repository.jdbc;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;

import java.util.Arrays;

public class DefaultAccountRepository implements AccountRepository {

    private static final String SELECT_BY_EMAIL = "SELECT * FROM accounts WHERE email=?";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAccountRepository.class);

    private final JDBCClient jdbcClient;

    public DefaultAccountRepository(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Single<UpdateResult> create(Account account) {
        return jdbcClient.rxGetConnection()
            .flatMap(
                connection -> connection.rxUpdateWithParams(
                    "INSERT INTO accounts (email, name, surname, balance, currency) VALUES (?, ?, ?, ?, ?)",
                    new JsonArray(
                        Arrays.asList(
                            account.getEmail(), account.getName(), account.getSurname(), account.getBalance(), account.getCurrency().name()
                        )
                    ))
                    .doFinally(connection::close)
            );

    }

    @Override
    public Single<UpdateResult> update(Account account) {
        return jdbcClient.rxGetConnection()
            .flatMap(
                connection -> connection.rxUpdateWithParams(
                    "UPDATE accounts SET name=?, surname=?, balance=?, currency=? WHERE email=?",
                    new JsonArray(
                        Arrays.asList(
                            account.getName(), account.getSurname(), account.getBalance(), account.getCurrency().name(), account.getEmail()
                        )
                    ))
                    .doFinally(connection::close)
            );

    }

    @Override
    public Single<Account> getByEmail(String email) {
        return jdbcClient
            .rxGetConnection()
            .flatMap(
                connection -> connection.rxQuerySingleWithParams(SELECT_BY_EMAIL, new JsonArray().add(email))
                    .flatMap(result -> {
                        connection.close();
                        Account account = new Account();
                        account.setEmail(result.getString(0))
                            .setName(result.getString(1))
                            .setSurname(result.getString(2))
                            .setBalance(result.getDouble(3))
                            .setCurrency(Currency.valueOf(result.getString(4)));
                        return Single.just(account);
                    })
            );
    }

    public static DefaultAccountRepository of(JDBCClient jdbcClient) {
        return new DefaultAccountRepository(jdbcClient);
    }
}
