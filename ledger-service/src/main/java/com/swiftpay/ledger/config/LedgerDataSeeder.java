package com.swiftpay.ledger.config;

import com.swiftpay.ledger.adapter.out.persistence.entity.AccountEntity;
import com.swiftpay.ledger.adapter.out.persistence.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Seeds demo accounts when the schema is created from JPA entities (no Flyway).
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class LedgerDataSeeder implements CommandLineRunner {

    public static final UUID ALICE_ACCOUNT_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
    public static final UUID BOB_ACCOUNT_ID = UUID.fromString("b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");

    private final AccountJpaRepository accountRepository;

    @Override
    public void run(String... args) {
        seedAccount(ALICE_ACCOUNT_ID, "Alice Sender", new BigDecimal("10000.00"), "USD");
        seedAccount(BOB_ACCOUNT_ID, "Bob Receiver", new BigDecimal("2500.00"), "USD");
    }

    private void seedAccount(UUID id, String ownerName, BigDecimal balance, String currency) {
        if (accountRepository.existsById(id)) {
            return;
        }
        AccountEntity account = new AccountEntity();
        account.setId(id);
        account.setOwnerName(ownerName);
        account.setBalance(balance);
        account.setCurrency(currency);
        accountRepository.save(account);
        log.info("seeded_account id={} owner={} balance={}", id, ownerName, balance);
    }
}
