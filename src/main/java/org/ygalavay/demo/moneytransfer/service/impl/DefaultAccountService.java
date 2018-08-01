package org.ygalavay.demo.moneytransfer.service.impl;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.model.Account;
import org.ygalavay.demo.moneytransfer.model.MoneyLock;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.service.AccountService;

import java.math.BigDecimal;

public class DefaultAccountService implements AccountService {

    private AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Single<Account> getByEmail(String email) {
        return accountRepository.getByEmail(email);
    }

    @Override
    public BigDecimal getAvailableBalanceForAccount(Account account) {
        BigDecimal balance = BigDecimal.valueOf(account.getBalance());
        if (account.getLocks() != null && ! account.getLocks().isEmpty()) {
            for (MoneyLock lock : account.getLocks()) {
                BigDecimal lockAmount = BigDecimal.valueOf(lock.getAmount());
                balance = balance.subtract(lockAmount);
            }
        }
        return balance;
    }
}
