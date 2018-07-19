package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;

public interface CrudRepository<T> {
    Single<T> save(T item);

    Single<T> save(T item, SQLConnection connection);
}
