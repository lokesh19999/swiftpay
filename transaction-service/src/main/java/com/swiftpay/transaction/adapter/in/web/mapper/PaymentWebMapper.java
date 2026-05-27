package com.swiftpay.transaction.adapter.in.web.mapper;

import com.swiftpay.transaction.adapter.in.web.dto.PaymentRequestDto;
import com.swiftpay.transaction.adapter.in.web.dto.PaymentResponseDto;
import com.swiftpay.transaction.application.command.CreatePaymentCommand;
import com.swiftpay.transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class PaymentWebMapper {

    public CreatePaymentCommand toCommand(PaymentRequestDto request) {
        return new CreatePaymentCommand(
                request.idempotencyKey(),
                request.senderAccountId(),
                request.receiverAccountId(),
                request.amount(),
                request.currency()
        );
    }

    public PaymentResponseDto toResponse(Transaction transaction) {
        return new PaymentResponseDto(
                transaction.id(),
                transaction.idempotencyKey(),
                transaction.senderAccountId(),
                transaction.receiverAccountId(),
                transaction.amount(),
                transaction.currency(),
                transaction.status(),
                transaction.createdAt()
        );
    }
}
