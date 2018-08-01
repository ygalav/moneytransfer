package org.ygalavay.demo.moneytransfer.service;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;

import java.math.BigDecimal;

public interface AccountService {
    Single<Account> getByEmail(String email);

    BigDecimal getAvailableBalanceForAccount(Account account);
}
