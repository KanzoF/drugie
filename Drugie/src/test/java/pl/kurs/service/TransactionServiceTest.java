package pl.kurs.service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.kurs.Repository.AccountRepository;
import pl.kurs.Repository.TransactionRepository;
import pl.kurs.exceptions.NotEnoughMoneyException;
import pl.kurs.model.Account;
import pl.kurs.model.Transaction;
import pl.kurs.model.commands.CreateTransactionCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private CreateTransactionCommand command;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        sourceAccount = new Account("PL61109010140000071219812874", new BigDecimal("10000"), "User1");
        sourceAccount.setId(1L);
        sourceAccount.setUsername("User1");

        destinationAccount = new Account("PL61109010140000071219812875", new BigDecimal("15000"), "User2");
        destinationAccount.setId(2L);
        destinationAccount.setUsername("User2");

        command = new CreateTransactionCommand(1L, 2L, new BigDecimal("100"), "Przelew testowy");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSourceAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setTitle("Przelew testowy");
        transaction.setTransactionDate(LocalDateTime.now());

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("User1");

    }

    @Test
    void shouldMakeTransferSuccessfully() {
        when(accountRepository.findByIdsWithLock(anySet()))
                .thenReturn(Arrays.asList(sourceAccount, destinationAccount));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);


        Transaction result = transactionService.makeTransfer(command);


        Assertions.assertEquals(1L, result.getSourceAccount().getId());
        Assertions.assertEquals(2L, result.getDestinationAccount().getId());
        Assertions.assertEquals(0, new BigDecimal("9900").compareTo(sourceAccount.getBalance()));
        Assertions.assertEquals(0, new BigDecimal("15100").compareTo(destinationAccount.getBalance()));
        Assertions.assertEquals("Przelew testowy", result.getTitle());

    }

    @Test
    void shouldThrowNotEnoughMoneyExceptionWhenAccountBalanceIsTooLow() {
        Account sourceAccountWithInsufficientFunds = new Account("PL61109010140000071219812874", new BigDecimal("50"), "User1");
        sourceAccountWithInsufficientFunds.setId(1L);

        Account destinationAccount = new Account("PL61109010140000071219812875", new BigDecimal("15000"), "User2");
        destinationAccount.setId(2L);

        CreateTransactionCommand command = new CreateTransactionCommand(1L, 2L, new BigDecimal("100"), "Test Transfer");

        when(accountRepository.findByIdsWithLock(anySet()))
                .thenReturn(Arrays.asList(sourceAccountWithInsufficientFunds, destinationAccount));


        assertThrows(NotEnoughMoneyException.class, () -> transactionService.makeTransfer(command));
    }

}
