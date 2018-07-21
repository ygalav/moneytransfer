package org.ygalavay.demo.moneytransfer.model;

public class MoneyLock {

    private String id;

    private double amount;

    private Currency currency;

    private PaymentTransaction paymentTransaction;

    private Account account;

    public String getId() {
        return id;
    }

    public MoneyLock setId(String id) {
        this.id = id;
        return this;
    }

    public double getAmount() {
        return amount;
    }

    public MoneyLock setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public MoneyLock setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public MoneyLock setPaymentTransaction(PaymentTransaction paymentTransaction) {
        this.paymentTransaction = paymentTransaction;
        return this;
    }

    public Account getAccount() {
        return account;
    }

    public MoneyLock setAccount(Account account) {
        this.account = account;
        return this;
    }
}
