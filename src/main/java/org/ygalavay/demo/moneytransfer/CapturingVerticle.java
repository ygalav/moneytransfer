package org.ygalavay.demo.moneytransfer;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

public class CapturingVerticle extends AbstractVerticle {

    private Logger log = LoggerFactory.getLogger(CapturingVerticle.class);

    @Override
    public void start() throws Exception {
        super.start();

        vertx.eventBus().<String>consumer("transactionsToFulfill").handler(message -> {
            String transactionId = message.body();
            log.info(String.format("Starting fulfillment for transaction %s", transactionId));
        });
    }
}
