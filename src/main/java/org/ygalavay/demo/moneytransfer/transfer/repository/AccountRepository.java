package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;

public interface AccountRepository {

    Single<Account> getByEmail(String email);
}
