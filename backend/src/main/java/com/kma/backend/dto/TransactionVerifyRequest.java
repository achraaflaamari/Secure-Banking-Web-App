package com.kma.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class TransactionVerifyRequest {

    @NotNull
    private Long transactionId;

    @NotNull
    @Pattern(regexp = "^[0-9]{6}$")
    private String code;

    public TransactionVerifyRequest() {}

    public TransactionVerifyRequest(Long transactionId, String code) {
        this.transactionId = transactionId;
        this.code = code;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
