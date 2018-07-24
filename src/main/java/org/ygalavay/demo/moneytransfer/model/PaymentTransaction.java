package org.ygalavay.demo.moneytransfer.model;

public class PaymentTransaction {

    private String id;

    private Account sender;

    private Account recipient;

    private MoneyLock moneyLock;

    private PaymentTransactionStatus status;

    public String getId() {
        return id;
    }

    public PaymentTransaction setId(String id) {
        this.id = id;
        return this;
    }

    public Account getSender() {
        return sender;
    }

    public PaymentTransaction setSender(Account sender) {
        this.sender = sender;
        return this;
    }

    public Account getRecipient() {
        return recipient;
    }

    public PaymentTransaction setRecipient(Account recipient) {
        this.recipient = recipient;
        return this;
    }

    public MoneyLock getMoneyLock() {
        return moneyLock;
    }

    public PaymentTransaction setMoneyLock(MoneyLock moneyLock) {
        this.moneyLock = moneyLock;
        return this;
    }

    public PaymentTransactionStatus getStatus() {
        return status;
    }

    public PaymentTransaction setStatus(PaymentTransactionStatus status) {
        this.status = status;
        return this;
    }

    public static PaymentTransaction open(Account sender, Account recipient) {
        return new PaymentTransaction()
            .setSender(sender)
            .setRecipient(recipient)
            .setStatus(PaymentTransactionStatus.CREATED);
    }
}
