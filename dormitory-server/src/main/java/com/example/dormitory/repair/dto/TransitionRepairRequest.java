package com.example.dormitory.repair.dto;

import jakarta.validation.constraints.NotBlank;

public class TransitionRepairRequest {

    @NotBlank(message = "status required")
    private String status;

    private String content;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
