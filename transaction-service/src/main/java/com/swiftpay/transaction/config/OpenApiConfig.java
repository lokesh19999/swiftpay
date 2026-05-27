package com.swiftpay.transaction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transactionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SwiftPay Transaction Service API")
                        .description("Payment initiation and status API")
                        .version("v1")
                        .contact(new Contact().name("SwiftPay Platform").email("platform@swiftpay.io")));
    }
}
