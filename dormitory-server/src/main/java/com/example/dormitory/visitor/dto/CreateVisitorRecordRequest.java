package com.example.dormitory.visitor.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CreateVisitorRecordRequest {

    private Long studentId;

    @NotBlank(message = "visitorName required")
    private String visitorName;

    private String idNo;

    private String phone;

    private String visitReason;

    private LocalDateTime visitAt;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public void setVisitReason(String visitReason) {
        this.visitReason = visitReason;
    }

    public LocalDateTime getVisitAt() {
        return visitAt;
    }

    public void setVisitAt(LocalDateTime visitAt) {
        this.visitAt = visitAt;
    }
}
