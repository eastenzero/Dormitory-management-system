package com.example.dormitory.dorm.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDormBuildingRequest {

    @NotBlank(message = "code required")
    private String code;

    @NotBlank(message = "name required")
    private String name;

    @NotBlank(message = "genderLimit required")
    private String genderLimit;

    private String address;

    private String status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenderLimit() {
        return genderLimit;
    }

    public void setGenderLimit(String genderLimit) {
        this.genderLimit = genderLimit;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
