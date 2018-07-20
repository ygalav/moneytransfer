package org.ygalavay.demo.moneytransfer.repository;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;

public interface AccountRepository {

    Single<Account> getByEmail(String email);
}
