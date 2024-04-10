package pl.kurs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc postman;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetUsersWithTheirTransactionCountsFromFile() throws Exception {
        postman.perform(get("/api/v1/accounts/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.username == 'User1')].outgoingTransactionsCount").value(1))
                .andExpect(jsonPath("$.content[?(@.username == 'User2')].outgoingTransactionsCount").value(1));
    }


    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void shouldFindTransactionsByAmountWithExactValues() throws Exception {
        postman.perform(get("/api/v1/transactions/search")
                        .param("amountFrom", "200")
                        .param("amountTo", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 2')].amount").value(200.00))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 2')].transactionDate").value("2023-01-03T13:00:00"))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 3')].amount").value(200.00))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 3')].transactionDate").value("2023-01-02T13:00:00"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void shouldFindTransactionsByDateAndAmountWithExactValues() throws Exception {
        postman.perform(get("/api/v1/transactions/search")
                        .param("dateFrom", "2023-01-02T13:00:00")
                        .param("dateTo", "2023-01-02T13:00:00")
                        .param("amountFrom", "200")
                        .param("amountTo", "250"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 3')].amount").value(200.00))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 3')].transactionDate").value("2023-01-02T13:00:00"))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 4')].amount").value(250.00))
                .andExpect(jsonPath("$.content[?(@.title == 'Przelew 4')].transactionDate").value("2023-01-02T13:00:00"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnForbiddenWhenUserTriesToAccessAdminOnlyApi() throws Exception {
        postman.perform(get("/api/v1/accounts/users"))
                .andExpect(status().isForbidden());
    }

}