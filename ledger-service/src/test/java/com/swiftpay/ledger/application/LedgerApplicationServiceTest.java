package com.swiftpay.ledger.application;

import com.swiftpay.ledger.domain.exception.InsufficientBalanceException;
import com.swiftpay.ledger.domain.model.Account;
import com.swiftpay.ledger.domain.port.AccountRepositoryPort;
import com.swiftpay.ledger.domain.port.LedgerEntryRepositoryPort;
import com.swiftpay.ledger.domain.port.PaymentOutcomePublisherPort;
import com.swiftpay.shared.event.PaymentInitiatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerApplicationServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;
    @Mock
    private LedgerEntryRepositoryPort ledgerEntryRepository;
    @Mock
    private PaymentOutcomePublisherPort outcomePublisher;

    @InjectMocks
    private LedgerApplicationService ledgerService;

    @Test
    void processPayment_insufficientBalance_publishesFailed() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), senderId, receiverId,
                new BigDecimal("99999.00"), "USD", Instant.now()
        );

        when(ledgerEntryRepository.existsByTransactionId(event.transactionId())).thenReturn(false);
        when(accountRepository.findByIdForUpdate(senderId)).thenReturn(Optional.of(
                new Account(senderId, "Alice", new BigDecimal("100.00"), "USD")
        ));
        when(accountRepository.findByIdForUpdate(receiverId)).thenReturn(Optional.of(
                new Account(receiverId, "Bob", new BigDecimal("50.00"), "USD")
        ));

        ledgerService.processPayment(event);

        verify(outcomePublisher).publishFailed(eq(event), any());
        verify(outcomePublisher, never()).publishCompleted(any());
    }

    @Test
    void processPayment_success_publishesCompleted() {
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                UUID.randomUUID(), UUID.randomUUID(), senderId, receiverId,
                new BigDecimal("10.00"), "USD", Instant.now()
        );

        when(ledgerEntryRepository.existsByTransactionId(event.transactionId())).thenReturn(false);
        when(accountRepository.findByIdForUpdate(senderId)).thenReturn(Optional.of(
                new Account(senderId, "Alice", new BigDecimal("100.00"), "USD")
        ));
        when(accountRepository.findByIdForUpdate(receiverId)).thenReturn(Optional.of(
                new Account(receiverId, "Bob", new BigDecimal("50.00"), "USD")
        ));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ledgerEntryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ledgerService.processPayment(event);

        verify(outcomePublisher).publishCompleted(event);
    }
}
