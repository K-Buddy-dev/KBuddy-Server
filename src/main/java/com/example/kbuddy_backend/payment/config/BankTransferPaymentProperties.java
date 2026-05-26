package com.example.kbuddy_backend.payment.config;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.bank-transfer")
public class BankTransferPaymentProperties {

    private String bankName = "";
    private String accountNumber = "";
    private String accountHolder = "KBuddy";
    private int depositTimeoutHours = 24;
    private BigDecimal platformFeeRate = new BigDecimal("15.00");
}
