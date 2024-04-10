package pl.kurs.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.model.dto.AccountWithTransactionsCountDto;
import pl.kurs.service.AccountService;
import org.springframework.data.domain.Pageable;


@RestController
@RequestMapping("api/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/users")
    public ResponseEntity<Page<AccountWithTransactionsCountDto>> getUsersWithTransactionCount(@PageableDefault Pageable pageable) {
        log.info("findAll With Transaction Count");
        Page<AccountWithTransactionsCountDto> accountDtos = accountService.getUsersWithTransactionCount(pageable);
        return ResponseEntity.ok(accountDtos);
    }

}



