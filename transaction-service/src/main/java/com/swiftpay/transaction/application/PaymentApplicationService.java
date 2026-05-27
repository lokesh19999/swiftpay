package com.swiftpay.transaction.application;

import com.swiftpay.shared.domain.TransactionStatus;
import com.swiftpay.transaction.application.command.CreatePaymentCommand;
import com.swiftpay.transaction.domain.exception.DuplicatePaymentException;
import com.swiftpay.transaction.domain.model.Transaction;
import com.swiftpay.transaction.domain.port.IdempotencyPort;
import com.swiftpay.transaction.domain.port.PaymentEventPublisherPort;
import com.swiftpay.transaction.domain.port.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final TransactionRepositoryPort transactionRepository;
    private final IdempotencyPort idempotencyPort;
    private final PaymentEventPublisherPort eventPublisher;

    @Transactional
    public Transaction initiatePayment(CreatePaymentCommand command) {
        UUID idempotencyKey = command.idempotencyKey();

        var existing = idempotencyPort.findExistingTransactionId(idempotencyKey);
        if (existing.isPresent()) {
            return transactionRepository.findById(existing.get())
                    .orElseThrow(() -> new DuplicatePaymentException(idempotencyKey));
        }

        UUID transactionId = UUID.randomUUID();
        if (!idempotencyPort.tryRegister(idempotencyKey, transactionId)) {
            UUID existingId = idempotencyPort.findExistingTransactionId(idempotencyKey)
                    .orElseThrow(() -> new DuplicatePaymentException(idempotencyKey));
            return transactionRepository.findById(existingId)
                    .orElseThrow(() -> new DuplicatePaymentException(idempotencyKey));
        }

        Instant now = Instant.now();
        Transaction transaction = new Transaction(
                transactionId,
                idempotencyKey,
                command.senderAccountId(),
                command.receiverAccountId(),
                command.amount(),
                command.currency(),
                TransactionStatus.PENDING,
                null,
                now,
                now
        );

        Transaction saved = transactionRepository.save(transaction);
        eventPublisher.publishPaymentInitiated(saved);

        log.info("payment_initiated transactionId={} idempotencyKey={} amount={} currency={}",
                saved.id(), saved.idempotencyKey(), saved.amount(), saved.currency());

        return saved;
    }

    public Optional<Transaction> getTransaction(UUID transactionId) {
        return transactionRepository.findById(transactionId);
    }

    @Transactional
    public void markCompleted(UUID transactionId) {
        updateStatus(transactionId, TransactionStatus.COMPLETED, null);
    }

    @Transactional
    public void markFailed(UUID transactionId, String reason) {
        updateStatus(transactionId, TransactionStatus.FAILED, reason);
    }

    private void updateStatus(UUID transactionId, TransactionStatus status, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        if (transaction.status() == TransactionStatus.COMPLETED
                || transaction.status() == TransactionStatus.FAILED) {
            log.debug("transaction_already_terminal transactionId={} status={}", transactionId, transaction.status());
            return;
        }

        Transaction updated = transaction.withStatus(status, reason);
        transactionRepository.save(updated);
        log.info("transaction_status_updated transactionId={} status={}", transactionId, status);
    }
}
