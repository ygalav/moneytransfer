package org.ygalavay.demo.moneytransfer.transfer.model;

import java.util.List;

public class Account {

    private String email;

    private String name;

    private String surname;

    private Double balance;

    private Currency currency;

    private List<MoneyLock> locks;

    public String getEmail() {
        return email;
    }

    public Account setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    public String getSurname() {
        return surname;
    }

    public Account setSurname(String surname) {
        this.surname = surname;
        return this;
    }

    public Double getBalance() {
        return balance;
    }

    public Account setBalance(Double balance) {
        this.balance = balance;
        return this;
    }

    public List<MoneyLock> getLocks() {
        return locks;
    }

    public Account setLocks(List<MoneyLock> locks) {
        this.locks = locks;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Account setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }
}
