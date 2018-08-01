package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;

import java.util.List;

public interface MoneyLockRepository extends CrudRepository<MoneyLock> {

    @Override
    Single<MoneyLock> create(MoneyLock moneyLock);

    Single<MoneyLock> update(MoneyLock moneyLock);

    Single<MoneyLock> update(MoneyLock moneyLock, SQLConnection connection);

    Single<List<MoneyLock>> findActiveLocksForAccount(Account account);
}
