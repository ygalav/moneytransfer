package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.transfer.model.MoneyLock;

public interface MoneyLockRepository extends CrudRepository<MoneyLock> {

    @Override
    Single<MoneyLock> save(MoneyLock moneyLock);
}
