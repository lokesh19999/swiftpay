package com.swiftpay.transaction.adapter.in.web;

import com.swiftpay.transaction.adapter.in.web.dto.PaymentRequestDto;
import com.swiftpay.transaction.adapter.in.web.dto.PaymentResponseDto;
import com.swiftpay.transaction.adapter.in.web.mapper.PaymentWebMapper;
import com.swiftpay.transaction.application.PaymentApplicationService;
import com.swiftpay.transaction.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initiation and status")
public class PaymentController {

    private final PaymentApplicationService paymentService;
    private final PaymentWebMapper mapper;

    @PostMapping
    @Operation(summary = "Initiate a payment", description = "Creates a PENDING transaction and publishes PaymentInitiated event")
    public ResponseEntity<PaymentResponseDto> createPayment(@Valid @RequestBody PaymentRequestDto request) {
        Transaction transaction = paymentService.initiatePayment(mapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(mapper.toResponse(transaction));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get payment status by transaction ID")
    public ResponseEntity<PaymentResponseDto> getPayment(@PathVariable UUID transactionId) {
        return paymentService.getTransaction(transactionId)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
