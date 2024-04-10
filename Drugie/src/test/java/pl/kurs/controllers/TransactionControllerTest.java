package pl.kurs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.Repository.AccountRepository;
import pl.kurs.model.commands.CreateTransactionCommand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc postman;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

//    @Test
//    public void shouldHandleConcurrentTransfersCorrectly() throws Exception {
//        int numberOfThreads = 20;
//        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
//        CountDownLatch latch = new CountDownLatch(numberOfThreads);
//        BigDecimal transferAmount = new BigDecimal("10.0");
//        List<Future<BigDecimal>> futures = new ArrayList<>();
//
//        for (int i = 0; i < numberOfThreads; i++) {
//            futures.add(service.submit(() -> {
//                try {
//                    CreateTransactionCommand command = new CreateTransactionCommand(1L, 2L, transferAmount, "Concurrent Transfer");
//                    postman.perform(post("/api/v1/transactions/transfer")
//                                    .contentType(MediaType.APPLICATION_JSON)
//                                    .content(objectMapper.writeValueAsString(command))
//                                    .with(csrf())
//                                    .with(user("User1").roles("USER")))
//                            .andExpect(status().isOk());
//                } catch (Exception e) {
//                    throw new IllegalStateException(e);
//                } finally {
//                    latch.countDown();
//                }
//                return accountRepository.findById(2L).orElseThrow().getBalance();
//            }));
//        }
//
//        latch.await(); // Wait for all threads to finish
//        service.shutdown();
//
//        BigDecimal finalBalance = accountRepository.findById(2L).orElseThrow().getBalance();
//        System.out.println("Final balance of destination account: " + finalBalance);
//
//        // Assert that all futures returned the same balance, indicating no lost updates
//        BigDecimal expectedBalance = futures.get(0).get();
//        assertTrue(futures.stream().allMatch(future -> {
//            try {
//                return future.get().compareTo(expectedBalance) == 0;
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }));
//    }
@Test
public void shouldHandleConcurrentTransfersCorrectly() throws Exception {
    int numberOfThreads = 20;
    ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);
    BigDecimal transferAmount = new BigDecimal("10.0");

    // Pobranie początkowego salda dla konta docelowego
    BigDecimal startingBalance = accountRepository.findById(2L).orElseThrow().getBalance();
    System.out.println("Starting balance of destination account: " + startingBalance);

    List<Future<BigDecimal>> futures = new ArrayList<>();

    for (int i = 0; i < numberOfThreads; i++) {
        futures.add(service.submit(() -> {
            try {
                CreateTransactionCommand command = new CreateTransactionCommand(1L, 2L, transferAmount, "Concurrent Transfer");
                postman.perform(post("/api/v1/transactions/transfer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command))
                                .with(csrf())
                                .with(user("User1").roles("USER")))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                latch.countDown();
            }
            return accountRepository.findById(2L).orElseThrow().getBalance();
        }));
    }

    latch.await(); // Wait for all threads to finish
    service.shutdown();

    BigDecimal finalBalance = accountRepository.findById(2L).orElseThrow().getBalance();
    System.out.println("Final balance of destination account: " + finalBalance);

    BigDecimal expectedBalance = startingBalance.add(transferAmount.multiply(new BigDecimal(numberOfThreads)));
    System.out.println("Expected final balance of destination account: " + expectedBalance);

    // Sprawdzenie, czy saldo końcowe jest zgodne z oczekiwanym
    Assertions.assertEquals(expectedBalance, finalBalance, "The final balance of the destination account does not match the expected value.");

    // Sprawdzenie, czy wszystkie przyszłe saldo zgadzają się z oczekiwanym
    boolean allMatch = futures.stream().allMatch(future -> {
        try {
            return future.get().compareTo(expectedBalance) == 0;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    });

    Assertions.assertTrue(allMatch, "Not all parallel transfers resulted in the expected final balance. This may indicate a race condition.");
}


    @Test
    public void shouldSuccessfullyMakeTransferAndReflectChangesInBalances11() throws Exception {
        BigDecimal balanceBeforeSource = accountRepository.findById(1L).orElseThrow().getBalance();
        BigDecimal balanceBeforeDest = accountRepository.findById(2L).orElseThrow().getBalance();

        BigDecimal transferAmount = new BigDecimal("1000.0");

        CreateTransactionCommand command = new CreateTransactionCommand(1L, 2L, transferAmount, "Test Transfer");

        postman.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command))
                        .with(csrf())
                        .with(user("User1").roles("USER")))
                .andExpect(status().isOk());

        BigDecimal balanceAfterSource = accountRepository.findById(1L).orElseThrow().getBalance();
        BigDecimal balanceAfterDest = accountRepository.findById(2L).orElseThrow().getBalance();

        assertEquals(balanceBeforeSource.subtract(transferAmount), balanceAfterSource);
        assertEquals(balanceBeforeDest.add(transferAmount), balanceAfterDest);
    }

    @Test
    void shouldMakeTransfer_whenAuthenticatedAsUser() throws Exception {
        CreateTransactionCommand command = new CreateTransactionCommand(1L, 2L, new BigDecimal("100.0"), "Test Transfer");
        postman.perform(post("/api/v1/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command))
                        .with(httpBasic("User1", "password1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value("100.0"))
                .andExpect(jsonPath("$.title").value("Test Transfer"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldFindTransactionsForUser1FromDataSqlFile() throws Exception {
        Long userId = 1L;                                                               //  User1 from data.sql

        postman.perform(get("/api/v1/transactions/search")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].sourceAccountId").value(userId))
                .andExpect(jsonPath("$.content[0].amount").value(100.00));
    }

    @Test
    void shouldFilterTransactionsByEndDateCorrectly() throws Exception {
        String dateTo = "2023-01-02T23:59:59";

        postman.perform(get("/api/v1/transactions/search")
                        .param("dateTo", dateTo)
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].transactionDate", everyItem(lessThanOrEqualTo(dateTo))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldMatchTransactionDetailsExactlyFromSqlFile() throws Exception {

        Long user1Id = 1L;
        Long user2Id = 2L;


        postman.perform(get("/api/v1/transactions/search")
                        .param("userId", user1Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.sourceAccountId == 1 && @.destinationAccountId == 2 && @.amount == 100.00)]").exists())
                .andExpect(jsonPath("$.content[?(@.transactionDate == '2023-01-04T12:00:00')]").exists());


        postman.perform(get("/api/v1/transactions/search")
                        .param("userId", user2Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.sourceAccountId == 2 && @.destinationAccountId == 1 && @.amount == 200.00)]").exists())
                .andExpect(jsonPath("$.content[?(@.transactionDate == '2023-01-03T13:00:00')]").exists());
    }


}