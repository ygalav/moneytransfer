package org.ygalavay.demo.moneytransfer.dto;

import org.ygalavay.demo.moneytransfer.model.Currency;

public class TransferRequest {

    private String sender;

    private String recipient;

    private Double amount;

    private Currency currency;

    public String getSender() {
        return sender;
    }

    public TransferRequest setSender(String sender) {
        this.sender = sender;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public TransferRequest setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public String getRecipient() {
        return recipient;
    }

    public TransferRequest setRecipient(String recipient) {
        this.recipient = recipient;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransferRequest setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }
}
