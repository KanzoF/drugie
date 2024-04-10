package pl.kurs.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kurs.model.Transaction;
import pl.kurs.model.dto.TransactionSearchCriteria;
import pl.kurs.model.commands.CreateTransactionCommand;
import pl.kurs.model.dto.TransactionDto;
import pl.kurs.service.TransactionService;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> makeTransfer(@Valid @RequestBody CreateTransactionCommand command) {
        Transaction transaction = transactionService.makeTransfer(command);
        TransactionDto transactionDto = TransactionDto.from(transaction);
        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TransactionDto>> searchTransactions(TransactionSearchCriteria criteria, Pageable pageable) {
        Page<Transaction> transactions = transactionService.findTransactions(criteria, pageable);
        Page<TransactionDto> transactionDtos = transactions.map(TransactionDto::from);
        return ResponseEntity.ok(transactionDtos);
    }

}
