package org.ygalavay.demo.moneytransfer.service;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.Currency;
import org.ygalavay.demo.moneytransfer.model.PaymentTransaction;

public interface PaymentTransactionService {

    Single<PaymentTransaction> openPaymentTransaction(final Account sender, final Account recipient,
                                                      final Currency currency, final Double amount);

}
