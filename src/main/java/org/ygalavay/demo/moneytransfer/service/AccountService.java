package org.ygalavay.demo.moneytransfer.service;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;

public interface AccountService {
    Single<Account> getByEmail(String email);
}
