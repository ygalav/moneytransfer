package org.ygalavay.demo.moneytransfer.transfer.configuration;

import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.DefaultAccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.DefaultTransferService;
import org.ygalavay.demo.moneytransfer.transfer.service.TransferService;

public class DependencyManager {

    public static TransferService createTransferService(final JDBCClient jdbcClient) {
        AccountRepository accountRepository = new DefaultAccountRepository(jdbcClient);
        DefaultTransferService transferService = new DefaultTransferService(accountRepository);
        return transferService;
    }
}
