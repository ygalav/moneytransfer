package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;

public interface CrudRepository<T> {

    Single<T> create(T item);

    Single<T> create(T item, SQLConnection connection);
}
