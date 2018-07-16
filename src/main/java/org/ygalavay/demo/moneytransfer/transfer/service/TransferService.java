package org.ygalavay.demo.moneytransfer.transfer.service;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;

public interface TransferService {

    Single<TransferResponse> authorize(TransferRequest transferRequest);

}
