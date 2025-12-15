package com.kma.backend.dto;

import java.math.BigDecimal;

public class CompteDTO {
    private Long id;
    private Long userId;
    private BigDecimal solde;
    private Boolean isActive;

    public CompteDTO() {}

    public CompteDTO(Long id, Long userId, BigDecimal solde, Boolean isActive) {
        this.id = id;
        this.userId = userId;
        this.solde = solde;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
