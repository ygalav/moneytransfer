package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import io.vertx.reactivex.ext.sql.SQLConnection;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;

public interface PaymentTransactionRepository {

    Single<PaymentTransaction> save(PaymentTransaction transaction);

    Single<PaymentTransaction> save(PaymentTransaction transaction, SQLConnection connection);

    Single<PaymentTransaction> findById(final String id);
}
