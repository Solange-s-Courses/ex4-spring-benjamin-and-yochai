package com.example.ex4.dto;

public class LoginForm {
    private String username;

    /**
     * Default constructor
     */
    public LoginForm() {}

    /**
     * Constructor with username
     * 
     * @param username Username to set
     */
    public LoginForm(String username) {
        this.username = username;
    }

    /**
     * Gets the username
     * 
     * @return Username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username
     * 
     * @param username Username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
} 