package com.example.demo.dto;

import javax.validation.constraints.NotBlank;

public class EmailConfirmationRequest {
    @NotBlank
    private String confirmationCode;

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}
