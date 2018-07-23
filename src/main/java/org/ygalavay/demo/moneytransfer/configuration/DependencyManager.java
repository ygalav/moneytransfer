package org.ygalavay.demo.moneytransfer.configuration;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.facade.DefaultTransferFacade;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultAccountRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultPaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultMoneyLockRepository;
import org.ygalavay.demo.moneytransfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;
import org.ygalavay.demo.moneytransfer.service.impl.DefaultAccountService;
import org.ygalavay.demo.moneytransfer.service.impl.DefaultPaymentTransactionService;

public class DependencyManager {

    public static TransferFacade createTransferService(Vertx vertx, JsonObject config) {
        JDBCClient jdbcClient = JDBCClient.createShared(vertx, config, "MoneyTransfer-Collection");
        AccountRepository accountRepository = new DefaultAccountRepository(jdbcClient);
        AccountService accountService = new DefaultAccountService(accountRepository);
        PaymentTransactionRepository paymentTransactionRepository = new DefaultPaymentTransactionRepository(jdbcClient);
        MoneyLockRepository moneyLockRepository = new DefaultMoneyLockRepository(jdbcClient);

        PaymentTransactionService transactionService =
            new DefaultPaymentTransactionService(jdbcClient, paymentTransactionRepository, moneyLockRepository);

        DefaultTransferFacade transferFacade =
            new DefaultTransferFacade(vertx, config, accountService, transactionService);
        return transferFacade;
    }
}
