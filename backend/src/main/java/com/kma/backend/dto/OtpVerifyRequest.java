package com.kma.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OtpVerifyRequest {

    @NotBlank
    @Size(max = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String code;

    public OtpVerifyRequest() {}

    public OtpVerifyRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
