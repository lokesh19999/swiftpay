package com.swiftpay.ledger.adapter.out.persistence;

import com.swiftpay.ledger.adapter.out.persistence.entity.AccountEntity;
import com.swiftpay.ledger.adapter.out.persistence.repository.AccountJpaRepository;
import com.swiftpay.ledger.domain.model.Account;
import com.swiftpay.ledger.domain.port.AccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final AccountJpaRepository repository;

    @Override
    public Optional<Account> findByIdForUpdate(UUID accountId) {
        return repository.findByIdForUpdate(accountId).map(this::toDomain);
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = repository.findById(account.id())
                .orElseGet(AccountEntity::new);
        entity.setId(account.id());
        entity.setOwnerName(account.ownerName());
        entity.setBalance(account.balance());
        entity.setCurrency(account.currency());
        return toDomain(repository.save(entity));
    }

    private Account toDomain(AccountEntity entity) {
        return new Account(entity.getId(), entity.getOwnerName(), entity.getBalance(), entity.getCurrency());
    }
}
