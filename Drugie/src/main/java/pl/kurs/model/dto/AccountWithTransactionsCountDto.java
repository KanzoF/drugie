package pl.kurs.model.dto;

import pl.kurs.model.Account;

public record AccountWithTransactionsCountDto(Long id, String username, int outgoingTransactionsCount) {
    public static AccountWithTransactionsCountDto createFrom(Account account) {
        int outgoingTransactionsCount = account.getOutgoingTransactions().size();
        return new AccountWithTransactionsCountDto(account.getId(), account.getUsername(), outgoingTransactionsCount);
    }

}
