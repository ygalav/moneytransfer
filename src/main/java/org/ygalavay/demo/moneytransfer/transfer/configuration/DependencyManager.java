package org.ygalavay.demo.moneytransfer.transfer.configuration;

import io.vertx.reactivex.core.Vertx;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.DummyAccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.DefaultTransferService;
import org.ygalavay.demo.moneytransfer.transfer.service.TransferService;

public class DependencyManager {

    public static TransferService createTransferService(final Vertx vertx) {
        AccountRepository accountRepository = new DummyAccountRepository();
        DefaultTransferService transferService = new DefaultTransferService();
        transferService.setAccountRepository(accountRepository);
        return transferService;
    }
}
