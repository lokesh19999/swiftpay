package com.swiftpay.transaction.integration;

import com.swiftpay.shared.domain.TransactionStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.jayway.jsonpath.JsonPath;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"payment.initiated", "payment.completed", "payment.failed"})
@Tag("integration")
class PaymentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("swiftpay_transactions")
            .withUsername("swiftuser")
            .withPassword("swiftpassword");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createPayment_returnsPending() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        String body = """
                {
                  "idempotencyKey": "%s",
                  "senderAccountId": "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                  "receiverAccountId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22",
                  "amount": 10.00,
                  "currency": "USD"
                }
                """.formatted(idempotencyKey);

        String response = mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value(TransactionStatus.PENDING.name()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String transactionId = JsonPath.read(response, "$.transactionId");

        mockMvc.perform(get("/v1/payments/" + transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TransactionStatus.PENDING.name()));
    }
}
