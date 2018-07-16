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

    public static Account of(String email, String name, String surname, Double balance, Currency currency) {
        return new Account().setName(name).setSurname(surname).setEmail(email).setBalance(balance).setCurrency(currency);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Account{");
        sb.append("email='").append(email).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", surname='").append(surname).append('\'');
        sb.append(", balance=").append(balance);
        sb.append(", currency=").append(currency);
        sb.append(", locks=").append(locks);
        sb.append('}');
        return sb.toString();
    }
}
