package org.ygalavay.demo.moneytransfer;

import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferResponse;
import org.ygalavay.demo.moneytransfer.transfer.dto.TransferRequest;

public interface TransferFacade {

    Single<TransferResponse> authorize(TransferRequest transferRequest);

}
