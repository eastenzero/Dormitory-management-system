package com.example.dormitory.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "username required")
    @Size(min = 3, max = 64, message = "username length 3-64")
    private String username;

    @NotBlank(message = "password required")
    @Size(min = 6, max = 64, message = "password length 6-64")
    private String password;

    private String realName;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
