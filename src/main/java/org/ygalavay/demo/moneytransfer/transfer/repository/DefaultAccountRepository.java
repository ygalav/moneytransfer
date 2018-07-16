package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;

import java.util.Arrays;

public class DefaultAccountRepository implements AccountRepository {

    private static final String SELECT_BY_EMAIL = "SELECT * FROM accounts WHERE email=?";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAccountRepository.class);

    private final JDBCClient jdbcClient;

    public DefaultAccountRepository(JDBCClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Single<UpdateResult> createAccount(Account account) {
        LOG.info("Creating new user " + account.toString());
        return jdbcClient.rxGetConnection()
            .flatMap(
                connection -> connection.rxUpdateWithParams(
                    "INSERT INTO accounts (email, name, surname, balance, currency) VALUES (?, ?, ?, ?, ?)",
                    new JsonArray(
                        Arrays.asList(
                            account.getEmail(), account.getName(), account.getSurname(), account.getBalance(), account.getCurrency().name()
                        )
                    ))
                    .doOnSuccess(updateResult -> connection.close())
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
