package pl.kurs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.kurs.Repository.AccountRepository;
import pl.kurs.model.Account;
import pl.kurs.model.dto.AccountWithTransactionsCountDto;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Page<AccountWithTransactionsCountDto> getUsersWithTransactionCount(Pageable pageable) {
        List<Account> accounts = accountRepository.findAllWithTransactions();
        List<AccountWithTransactionsCountDto> dtos = accounts.stream()
                .map(AccountWithTransactionsCountDto::createFrom)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, dtos.size());
    }

}


