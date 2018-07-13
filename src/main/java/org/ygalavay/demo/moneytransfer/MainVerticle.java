package org.ygalavay.demo.moneytransfer;

import io.vertx.core.AbstractVerticle;
import org.ygalavay.demo.moneytransfer.transfer.MoneyTransferVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(MoneyTransferVerticle.class.getName());
    }
}
