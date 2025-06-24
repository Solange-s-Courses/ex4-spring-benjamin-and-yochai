package com.example.ex4.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

// DTO for registration form
public class RegistrationForm {

    @NotBlank(message = "שם משתמש הוא שדה חובה")
    @Size(min = 3, max = 20, message = "שם המשתמש חייב להיות בין 3 ל-20 תווים")
    private String username;

    @NotBlank(message = "סיסמה היא שדה חובה")
    @Size(min = 6, message = "הסיסמה חייבת להכיל לפחות 6 תווים")
    private String password;

    @NotBlank(message = "אימייל הוא שדה חובה")
    @Email(message = "אנא הכנס כתובת אימייל תקינה")
    private String email;

    private boolean commander;

    @Enumerated(EnumType.STRING)
    private MultipartFile militaryIdDoc;


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

    public boolean isCommander() {
        return commander;
    }

    public void setCommander(boolean commander) {
        this.commander = commander;
    }

    public MultipartFile getMilitaryIdDoc() {
        return militaryIdDoc;
    }

    public void setMilitaryIdDoc(MultipartFile militaryIdDoc) {
        this.militaryIdDoc = militaryIdDoc;
    }
}
