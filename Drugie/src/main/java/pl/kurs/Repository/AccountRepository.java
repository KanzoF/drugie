package pl.kurs.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pl.kurs.model.Account;

import java.util.List;
import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id IN :accountIds")
    List<Account> findByIdsWithLock(Set<Long> accountIds);

    @EntityGraph(attributePaths = {"outgoingTransactions", "incomingTransactions"})
    @Query("select a from Account a")
    List<Account> findAllWithTransactions();
}
