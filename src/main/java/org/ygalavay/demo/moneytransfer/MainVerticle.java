package org.ygalavay.demo.moneytransfer;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    private Logger log = LoggerFactory.getLogger(MainVerticle.class);
    private static final int WORKER_POOL_SIZE = 200;

    @Override
    public void start(Future<Void> done) throws Exception {

        DeploymentOptions serverOpts = new DeploymentOptions()
            .setWorkerPoolSize(WORKER_POOL_SIZE)
            .setConfig(config());

        DeploymentOptions workerOpts = new DeploymentOptions()
            .setWorker(true)
            .setMultiThreaded(true)
            .setWorkerPoolSize(WORKER_POOL_SIZE)
            .setConfig(config());

        CompositeFuture.all(
            deploy(CapturingVerticle.class.getName(), workerOpts),
            deploy(AuthorizationVerticle.class.getName(), serverOpts)
        ).setHandler(r -> {
            if(r.succeeded()){
                done.complete();
            }
            else {
                done.fail(r.cause());
            }
        });
    }

    private Future<Void> deploy(String name, DeploymentOptions opts){
        Future<Void> done = Future.future();

        vertx.deployVerticle(name, opts, res -> {
            if(res.failed()){
                log.info("Failed to deploy verticle " + name);
                done.fail(res.cause());
            }
            else {
                log.info("Deployed verticle " + name);
                done.complete();
            }
        });

        return done;
    }
}
