package org.ygalavay.demo.moneytransfer.transfer.configuration;

import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.DefaultTransferFacade;
import org.ygalavay.demo.moneytransfer.TransferFacade;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.jdbc.DefaultAccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.jdbc.DefaultPaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.jdbc.DefaultMoneyLockRepository;
import org.ygalavay.demo.moneytransfer.transfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.transfer.service.PaymentTransactionService;
import org.ygalavay.demo.moneytransfer.transfer.service.impl.DefaultAccountService;
import org.ygalavay.demo.moneytransfer.transfer.service.impl.DefaultPaymentTransactionService;

public class DependencyManager {

    public static TransferFacade createTransferService(final JDBCClient jdbcClient) {
        AccountRepository accountRepository = new DefaultAccountRepository(jdbcClient);
        AccountService accountService = new DefaultAccountService(accountRepository);
        PaymentTransactionRepository paymentTransactionRepository = new DefaultPaymentTransactionRepository(jdbcClient);
        MoneyLockRepository moneyLockRepository = new DefaultMoneyLockRepository(jdbcClient);

        PaymentTransactionService transactionService = new DefaultPaymentTransactionService(jdbcClient, paymentTransactionRepository, moneyLockRepository);

        DefaultTransferFacade transferFacade = new DefaultTransferFacade(accountService, transactionService);
        return transferFacade;
    }
}
