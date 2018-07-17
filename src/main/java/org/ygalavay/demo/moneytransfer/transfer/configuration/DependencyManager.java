package org.ygalavay.demo.moneytransfer.transfer.configuration;

import io.reactivex.Single;
import io.vertx.reactivex.ext.jdbc.JDBCClient;
import org.ygalavay.demo.moneytransfer.transfer.model.Account;
import org.ygalavay.demo.moneytransfer.transfer.model.Currency;
import org.ygalavay.demo.moneytransfer.transfer.model.PaymentTransaction;
import org.ygalavay.demo.moneytransfer.transfer.repository.AccountRepository;
import org.ygalavay.demo.moneytransfer.transfer.repository.DefaultAccountRepository;
import org.ygalavay.demo.moneytransfer.DefaultTransferFacade;
import org.ygalavay.demo.moneytransfer.TransferFacade;
import org.ygalavay.demo.moneytransfer.transfer.service.AccountService;
import org.ygalavay.demo.moneytransfer.transfer.service.TransactionService;
import org.ygalavay.demo.moneytransfer.transfer.service.impl.DefaultAccountService;

public class DependencyManager {

    public static TransferFacade createTransferService(final JDBCClient jdbcClient) {
        AccountRepository accountRepository = new DefaultAccountRepository(jdbcClient);
        AccountService accountService = new DefaultAccountService(accountRepository);

        TransactionService transactionService = new TransactionService() {
            @Override
            public Single<PaymentTransaction> openPaymentTransaction(Account sender, Account recipient, Currency currency, Double amount) {
                return Single.just(new PaymentTransaction());
            }
        };

        DefaultTransferFacade transferFacade = new DefaultTransferFacade(accountService, transactionService);
        return transferFacade;
    }
}
