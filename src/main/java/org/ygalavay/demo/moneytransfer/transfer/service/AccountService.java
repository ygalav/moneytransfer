package org.ygalavay.demo.moneytransfer.transfer.service;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;

public interface AccountService {
    Single<Account> getByEmail(String email);
}
