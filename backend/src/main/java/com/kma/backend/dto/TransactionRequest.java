package com.kma.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class TransactionRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9\\-]+$")
    private String receiverRip;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    public TransactionRequest() {}

    public TransactionRequest(String receiverRip, BigDecimal amount) {
        this.receiverRip = receiverRip;
        this.amount = amount;
    }

    public String getReceiverRip() {
        return receiverRip;
    }

    public void setReceiverRip(String receiverRip) {
        this.receiverRip = receiverRip;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
