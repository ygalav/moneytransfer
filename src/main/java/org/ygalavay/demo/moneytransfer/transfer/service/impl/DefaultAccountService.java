package org.ygalavay.demo.moneytransfer.transfer.service.impl;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.AccountService;

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
