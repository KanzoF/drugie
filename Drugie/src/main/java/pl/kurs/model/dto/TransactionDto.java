package pl.kurs.model.dto;

import pl.kurs.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDto(Long id, Long sourceAccountId, Long destinationAccountId, BigDecimal amount, String title,
                             LocalDateTime transactionDate) {
    public static TransactionDto from(Transaction transaction) {
        return new TransactionDto(
                transaction.getId(),
                transaction.getSourceAccount().getId(),
                transaction.getDestinationAccount().getId(),
                transaction.getAmount(),
                transaction.getTitle(),
                transaction.getTransactionDate());
    }
}