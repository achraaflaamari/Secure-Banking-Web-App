package com.kma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordChangeVerifyRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String code;

    @NotBlank
    @Size(min = 6, max = 100)
    private String newPassword;

    public PasswordChangeVerifyRequest() {}

    public PasswordChangeVerifyRequest(String code, String newPassword) {
        this.code = code;
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
