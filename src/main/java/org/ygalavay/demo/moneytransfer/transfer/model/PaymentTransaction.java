package org.ygalavay.demo.moneytransfer.transfer.model;

import java.util.List;

public class PaymentTransaction {
    private String id;

    private Account sender;

    private Account recipient;

    private List<TransactionEntry> entries;

}
