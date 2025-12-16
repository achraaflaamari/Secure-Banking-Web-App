package com.kma.backend.dto;

public class TransactionInitiateResponse {
    private Long transactionId;
    private String message;

    public TransactionInitiateResponse() {}

    public TransactionInitiateResponse(Long transactionId, String message) {
        this.transactionId = transactionId;
        this.message = message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
