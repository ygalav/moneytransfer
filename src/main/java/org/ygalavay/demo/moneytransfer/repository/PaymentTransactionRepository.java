package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;

public interface PaymentTransactionRepository {

    Single<PaymentTransaction> create(PaymentTransaction transaction);

    Single<PaymentTransaction> update(PaymentTransaction transaction);

    Single<PaymentTransaction> update(PaymentTransaction transaction, SQLConnection connection);

    Single<PaymentTransaction> create(PaymentTransaction transaction, SQLConnection connection);

    Single<PaymentTransaction> findById(final String id);
}
