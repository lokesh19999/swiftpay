package com.swiftpay.transaction.application;

import com.swiftpay.shared.domain.TransactionStatus;
import com.swiftpay.transaction.application.command.CreatePaymentCommand;
import com.swiftpay.transaction.domain.model.Transaction;
import com.swiftpay.transaction.domain.port.IdempotencyPort;
import com.swiftpay.transaction.domain.port.PaymentEventPublisherPort;
import com.swiftpay.transaction.domain.port.TransactionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;
    @Mock
    private IdempotencyPort idempotencyPort;
    @Mock
    private PaymentEventPublisherPort eventPublisher;

    @InjectMocks
    private PaymentApplicationService paymentService;

    @Test
    void initiatePayment_persistsPendingAndPublishesEvent() {
        UUID idempotencyKey = UUID.randomUUID();
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

        when(idempotencyPort.findExistingTransactionId(idempotencyKey)).thenReturn(Optional.empty());
        when(idempotencyPort.tryRegister(org.mockito.ArgumentMatchers.eq(idempotencyKey), org.mockito.ArgumentMatchers.any(UUID.class))).thenReturn(true);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreatePaymentCommand command = new CreatePaymentCommand(
                idempotencyKey, sender, receiver, new BigDecimal("50.00"), "USD"
        );

        Transaction result = paymentService.initiatePayment(command);

        assertThat(result.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(result.amount()).isEqualByComparingTo("50.00");

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(eventPublisher).publishPaymentInitiated(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(result.id());
    }

    @Test
    void markCompleted_updatesTerminalStatus() {
        UUID txId = UUID.randomUUID();
        Transaction pending = new Transaction(
                txId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "USD", TransactionStatus.PENDING, null,
                Instant.now(), Instant.now()
        );
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(pending));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.markCompleted(txId);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(TransactionStatus.COMPLETED);
    }
}
