package org.ygalavay.demo.moneytransfer;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import org.ygalavay.demo.moneytransfer.configuration.Constants;
import org.ygalavay.demo.moneytransfer.configuration.DependencyManager;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;

import static org.ygalavay.demo.moneytransfer.configuration.Constants.EVENT_FULFILLMENT_UNKNOWN_ERROR;

public class CapturingVerticle extends AbstractVerticle {

    private Logger log = LoggerFactory.getLogger(CapturingVerticle.class);

    private TransferFacade transferFacade;

    @Override
    public void start() throws Exception {
        super.start();
        transferFacade = DependencyManager.getInstance(vertx, config()).getTransferFacade();

        vertx.eventBus().<String>consumer(config().getString(Constants.EVENT_DO_CAPTURE)).handler(message -> {
            String transactionId = message.body();
            log.info(String.format("Starting fulfillment for transaction %s", transactionId));
            transferFacade.fulfillTransaction(transactionId)
                .subscribe(
                    () -> log.info(String.format("Transaction [%s] fulfilled successfully", transactionId)),
                    error -> {
                        log.error(String.format("Fulfillment has failed for transaction [%s]", transactionId), error);
                        vertx.eventBus().publish(config().getString(EVENT_FULFILLMENT_UNKNOWN_ERROR), transactionId);
                    });
        });
    }
}
