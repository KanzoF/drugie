package pl.kurs.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionSearchCriteria {

    private Long userId;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

}
