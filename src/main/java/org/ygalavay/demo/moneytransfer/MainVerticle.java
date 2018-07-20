package org.ygalavay.demo.moneytransfer;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        vertx.deployVerticle(MoneyTransferVerticle.class.getName());
    }
}
