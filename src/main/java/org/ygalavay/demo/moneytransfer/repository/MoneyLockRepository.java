package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;

public interface MoneyLockRepository extends CrudRepository<MoneyLock> {

    @Override
    Single<MoneyLock> create(MoneyLock moneyLock);
}
