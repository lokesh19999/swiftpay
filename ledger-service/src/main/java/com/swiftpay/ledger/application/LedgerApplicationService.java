package com.swiftpay.ledger.application;

import com.swiftpay.ledger.domain.exception.InsufficientBalanceException;
import com.swiftpay.ledger.domain.model.Account;
import com.swiftpay.ledger.domain.model.LedgerEntry;
import com.swiftpay.ledger.domain.port.AccountRepositoryPort;
import com.swiftpay.ledger.domain.port.LedgerEntryRepositoryPort;
import com.swiftpay.ledger.domain.port.PaymentOutcomePublisherPort;
import com.swiftpay.shared.event.PaymentInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerApplicationService {

    private final AccountRepositoryPort accountRepository;
    private final LedgerEntryRepositoryPort ledgerEntryRepository;
    private final PaymentOutcomePublisherPort outcomePublisher;

    @Transactional
    public void processPayment(PaymentInitiatedEvent event) {
        if (ledgerEntryRepository.existsByTransactionId(event.transactionId())) {
            log.warn("ledger_idempotent_skip transactionId={}", event.transactionId());
            return;
        }

        try {
            Account sender = accountRepository.findByIdForUpdate(event.senderAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
            Account receiver = accountRepository.findByIdForUpdate(event.receiverAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

            validateCurrency(sender, receiver, event.currency());

            if (sender.balance().compareTo(event.amount()) < 0) {
                throw new InsufficientBalanceException(sender.id());
            }

            Account updatedSender = new Account(
                    sender.id(), sender.ownerName(),
                    sender.balance().subtract(event.amount()), sender.currency()
            );
            Account updatedReceiver = new Account(
                    receiver.id(), receiver.ownerName(),
                    receiver.balance().add(event.amount()), receiver.currency()
            );

            accountRepository.save(updatedSender);
            accountRepository.save(updatedReceiver);

            persistEntries(event);
            outcomePublisher.publishCompleted(event);

            log.info("ledger_transfer_success transactionId={} amount={}", event.transactionId(), event.amount());
        } catch (InsufficientBalanceException ex) {
            log.warn("ledger_insufficient_balance transactionId={} accountId={}",
                    event.transactionId(), ex.getAccountId());
            outcomePublisher.publishFailed(event, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("ledger_validation_failed transactionId={} reason={}", event.transactionId(), ex.getMessage());
            outcomePublisher.publishFailed(event, ex.getMessage());
        }
    }

    private void validateCurrency(Account sender, Account receiver, String currency) {
        if (!sender.currency().equals(currency) || !receiver.currency().equals(currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
    }

    private void persistEntries(PaymentInitiatedEvent event) {
        Instant now = Instant.now();
        ledgerEntryRepository.save(new LedgerEntry(
                UUID.randomUUID(), event.transactionId(), event.senderAccountId(),
                LedgerEntry.EntryType.DEBIT, event.amount(), event.currency(), now
        ));
        ledgerEntryRepository.save(new LedgerEntry(
                UUID.randomUUID(), event.transactionId(), event.receiverAccountId(),
                LedgerEntry.EntryType.CREDIT, event.amount(), event.currency(), now
        ));
    }
}
