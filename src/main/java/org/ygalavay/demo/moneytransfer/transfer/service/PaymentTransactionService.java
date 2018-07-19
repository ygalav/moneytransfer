package org.ygalavay.demo.moneytransfer.transfer.service;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;

public interface PaymentTransactionService {

    Single<PaymentTransaction> openPaymentTransaction(final Account sender, final Account recipient,
                                                      final Currency currency, final Double amount);

}
