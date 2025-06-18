package com.example.ex4.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "שם משתמש הוא שדה חובה")
    @Size(min = 3, max = 20, message = "שם המשתמש חייב להיות בין 3 ל-20 תווים")
    private String username;

    @NotBlank(message = "סיסמה היא שדה חובה")
    @Size(min = 6, message = "הסיסמה חייבת להכיל לפחות 6 תווים")
    private String password;

    @NotBlank(message = "אימייל הוא שדה חובה")
    @Email(message = "אנא הכנס כתובת אימייל תקינה")
    private String email;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
} 