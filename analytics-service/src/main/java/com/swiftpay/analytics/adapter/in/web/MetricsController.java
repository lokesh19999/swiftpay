package com.swiftpay.analytics.adapter.in.web;

import com.swiftpay.analytics.domain.model.PaymentMetric;
import com.swiftpay.analytics.domain.port.PaymentMetricRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Payment metrics read API")
public class MetricsController {

    private final PaymentMetricRepositoryPort metricRepository;

    @GetMapping("/payments")
    @Operation(summary = "List recent completed payment metrics")
    public List<PaymentMetric> recentPayments(@RequestParam(defaultValue = "50") int limit) {
        return metricRepository.findRecent(Math.min(limit, 200));
    }
}
