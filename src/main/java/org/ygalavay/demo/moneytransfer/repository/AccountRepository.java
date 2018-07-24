package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import io.vertx.ext.sql.UpdateResult;
import org.ygalavay.demo.moneytransfer.model.Account;

public interface AccountRepository {

    Single<UpdateResult> create(Account account);

    Single<UpdateResult> update(Account account);

    Single<Account> getByEmail(String email);
}
