package org.ygalavay.demo.moneytransfer.transfer.repository;

import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;

import java.util.HashMap;
import java.util.Map;

public class DummyAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts;

    public DummyAccountRepository() {
        accounts = new HashMap<>();

        accounts.put("account1@mail.com", new Account()
            .setEmail("account1@mail.com")
            .setName("John")
            .setSurname("Doe")
            .setBalance(100.0d)
            .setCurrency(Currency.USD)
        );

        accounts.put("ygalavay@mail.com", new Account()
            .setEmail("ygalavay@mail.com")
            .setName("Yuriy")
            .setSurname("Galavay")
            .setBalance(50.0d)
            .setCurrency(Currency.USD)
        );

        accounts.put("tgalavay@mail.com", new Account()
            .setEmail("tgalavay@mail.com")
            .setName("Tetyana")
            .setSurname("Galavay")
            .setBalance(30.0d)
            .setCurrency(Currency.EUR)
        );
    }

    @Override
    public Single<Account> getByEmail(final String email) {
        return Single.just(accounts.get(email));
    }
}
