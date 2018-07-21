package org.ygalavay.demo.moneytransfer.facade;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.ygalavay.demo.moneytransfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.dto.TransferRequest;

public interface TransferFacade {

    Single<TransferResponse> authorize(TransferRequest transferRequest);

    Completable fulfillTransaction(String transactionId);
}
