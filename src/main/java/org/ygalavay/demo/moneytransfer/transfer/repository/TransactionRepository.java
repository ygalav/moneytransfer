package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Completable;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;

public interface TransactionRepository {

    Completable save(PaymentTransaction transaction);

}
