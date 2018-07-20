package org.ygalavay.demo.moneytransfer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;

import static org.ygalavay.demo.moneytransfer.configuration.Constants.CAPTURE_MSG_NAME;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        DeploymentOptions options = new DeploymentOptions()
            .setConfig(config());
        vertx.eventBus()
            .<String>consumer(config().getString(CAPTURE_MSG_NAME))
            .handler(message -> {
                final String transactionId = message.body();
            });
        vertx.deployVerticle(AuthorizationVerticle.class.getName(), options);
    }
}
