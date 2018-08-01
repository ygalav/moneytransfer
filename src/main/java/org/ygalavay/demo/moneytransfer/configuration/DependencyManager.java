package org.ygalavay.demo.moneytransfer.configuration;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.facade.DefaultTransferFacade;
import org.ygalavay.demo.moneytransfer.facade.TransferFacade;
import org.ygalavay.demo.moneytransfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.repository.MoneyLockRepository;
import org.ygalavay.demo.moneytransfer.repository.PaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultAccountRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultMoneyLockRepository;
import org.ygalavay.demo.moneytransfer.repository.jdbc.DefaultPaymentTransactionRepository;
import org.ygalavay.demo.moneytransfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.service.PaymentTransactionService;
import org.ygalavay.demo.moneytransfer.service.impl.DefaultAccountService;
import org.ygalavay.demo.moneytransfer.service.impl.DefaultPaymentTransactionService;

public class DependencyManager {

    private static ThreadLocal<DependencyManager> THREAD_LOCAL = new ThreadLocal<>();


    private Vertx vertx;
    private JsonObject config;

    private JDBCClient jdbcClient;
    private AccountRepository accountRepository;
    private AccountService accountService;
    private PaymentTransactionRepository paymentTransactionRepository;
    private MoneyLockRepository moneyLockRepository;
    private PaymentTransactionService transactionService;
    private TransferFacade transferFacade;




    private DependencyManager(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.config = config;
    }

    public static DependencyManager getInstance(Vertx vertx, JsonObject config) {
        if (THREAD_LOCAL.get() == null) {
            THREAD_LOCAL.set(new DependencyManager(vertx, config));
        }
        return THREAD_LOCAL.get();
    }


    public JDBCClient getJdbcClient() {
        if (jdbcClient == null) {
            jdbcClient = JDBCClient.createShared(vertx, config, "MoneyTransfer-Collection");
        }
        return jdbcClient;
    }

    public AccountRepository getAccountRepository() {
        if (accountRepository == null) {
            accountRepository = new DefaultAccountRepository(getJdbcClient(), getMoneyLockRepository());
        }
        return accountRepository;
    }

    public AccountService getAccountService() {
        if (accountService == null) {
            accountService = new DefaultAccountService(getAccountRepository());
        }
        return accountService;
    }

    public PaymentTransactionRepository getPaymentTransactionRepository() {
        if (paymentTransactionRepository == null) {
            paymentTransactionRepository = new DefaultPaymentTransactionRepository(getJdbcClient());
        }
        return paymentTransactionRepository;
    }

    public MoneyLockRepository getMoneyLockRepository() {
        if (moneyLockRepository == null) {
            moneyLockRepository = new DefaultMoneyLockRepository(getJdbcClient());
        }
        return moneyLockRepository;
    }

    public PaymentTransactionService getTransactionService() {
        if (transactionService == null) {
            transactionService = new DefaultPaymentTransactionService(
                getJdbcClient(),
                getPaymentTransactionRepository(),
                getMoneyLockRepository(),
                getAccountRepository()
            );
        }
        return transactionService;
    }

    public TransferFacade getTransferFacade() {
        if (transferFacade == null) {
            transferFacade = new DefaultTransferFacade(vertx, config, getAccountService(), getTransactionService());
        }
        return transferFacade;
    }
}
