package com.example.dormitory.repair.dto;

import jakarta.validation.constraints.NotNull;

public class AssignRepairRequest {

    @NotNull(message = "assigneeUserId required")
    private Long assigneeUserId;

    private String content;

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
