package pl.kurs.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import pl.kurs.Repository.AccountRepository;
import pl.kurs.model.Account;
import org.springframework.data.domain.Pageable;
import pl.kurs.model.dto.AccountWithTransactionsCountDto;


import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void getUsersWithTransactionCountShouldReturnPageOfAccountsWithTransactionsCount() {
        List<Account> accounts = Arrays.asList(new Account(), new Account());
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findAllWithTransactions()).thenReturn(accounts);

        Page<AccountWithTransactionsCountDto> resultPage = accountService.getUsersWithTransactionCount(pageable);

        assertNotNull(resultPage);
        assertEquals(2, resultPage.getTotalElements());
        verify(accountRepository).findAllWithTransactions();
    }

}