package org.ygalavay.demo.moneytransfer.dto;

import org.ygalavay.demo.moneytransfer.model.AuthorizeResult;

public class TransferResponse {

    private AuthorizeResult result;
    private String message;

    public TransferResponse(AuthorizeResult result, String message) {
        this.result = result;
        this.message = message;
    }

    public TransferResponse() {
    }

    public AuthorizeResult getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public static TransferResponse CREATED =
        new TransferResponse(AuthorizeResult.ACCEPTED, "Transaction authorized successfully");

    public static TransferResponse CURRENCY_NOT_MATCH =
        new TransferResponse(AuthorizeResult.FAILED_CURRENCY_NOT_MATCH, "Currency Not Match");

    public static TransferResponse LOW_BALLANCE =
        new TransferResponse(AuthorizeResult.FAILED_LOW_BALANCE, "Not Enough Balance");

    public static TransferResponse UNKNOWN_EROOR =
        new TransferResponse(AuthorizeResult.ERROR, "Unknown Error");
}
