package com.swiftpay.transaction.adapter.out.redis;

import com.swiftpay.transaction.domain.port.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyAdapter implements IdempotencyPort {

    private static final String KEY_PREFIX = "swiftpay:idempotency:";

    private final StringRedisTemplate redisTemplate;

    @Value("${swiftpay.idempotency.ttl-hours:24}")
    private long ttlHours;

    @Override
    public Optional<UUID> findExistingTransactionId(UUID idempotencyKey) {
        String value = redisTemplate.opsForValue().get(key(idempotencyKey));
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(value));
    }

    @Override
    public boolean tryRegister(UUID idempotencyKey, UUID transactionId) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                key(idempotencyKey),
                transactionId.toString(),
                Duration.ofHours(ttlHours)
        );
        return Boolean.TRUE.equals(success);
    }

    private String key(UUID idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }
}
