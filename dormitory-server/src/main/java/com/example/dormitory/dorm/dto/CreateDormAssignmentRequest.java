package com.example.dormitory.dorm.dto;

import jakarta.validation.constraints.NotNull;

public class CreateDormAssignmentRequest {

    @NotNull(message = "studentId required")
    private Long studentId;

    @NotNull(message = "bedId required")
    private Long bedId;

    private String reason;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getBedId() {
        return bedId;
    }

    public void setBedId(Long bedId) {
        this.bedId = bedId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
