package com.example.ex4.dto;

public class LoginForm {
    private String username;

    public LoginForm() {}

    public LoginForm(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 