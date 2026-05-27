package com.swiftpay.ledger.domain.port;

import com.swiftpay.ledger.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {

    Optional<Account> findByIdForUpdate(UUID accountId);

    Account save(Account account);
}
