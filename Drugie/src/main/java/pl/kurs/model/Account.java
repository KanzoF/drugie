package pl.kurs.model;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 26, max = 26)
    private String numberAccount;

    @Min(0)
    private BigDecimal balance;

    @Size(min = 3, max = 15)
    private String username;


    @OneToMany(mappedBy = "sourceAccount")
    private Set<Transaction> outgoingTransactions = new HashSet<>();

    @OneToMany(mappedBy = "destinationAccount")
    private Set<Transaction> incomingTransactions = new HashSet<>();


    public Account(String numberAccount, BigDecimal balance, String username) {
        this.numberAccount = numberAccount;
        this.balance = balance;
        this.username = username;
    }
}