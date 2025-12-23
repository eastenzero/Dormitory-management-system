package com.example.dormitory.visitor.vo;

import java.time.LocalDateTime;

public class VisitorRecordVo {

    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;

    private String visitorName;
    private String idNo;
    private String phone;
    private String visitReason;
    private LocalDateTime visitAt;
    private LocalDateTime leaveAt;
    private String status;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public LocalDateTime getLeaveAt() {
        return leaveAt;
    }

    public void setLeaveAt(LocalDateTime leaveAt) {
        this.leaveAt = leaveAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
