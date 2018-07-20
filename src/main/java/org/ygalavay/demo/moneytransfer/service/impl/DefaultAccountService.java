package org.ygalavay.demo.moneytransfer.service.impl;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.service.AccountService;

public class DefaultAccountService implements AccountService {

    private AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Single<Account> getByEmail(String email) {
        return accountRepository.getByEmail(email);
    }
}
