package com.example.ex4.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.ex4.validation.FileSize;

public class RegistrationForm {

    @NotBlank(message = "שם משתמש הוא שדה חובה")
    @Size(min = 3, max = 20, message = "שם המשתמש חייב להיות בין 3 ל-20 תווים")
    private String username;

    @NotBlank(message = "שם פרטי הוא שדה חובה")
    @Size(min = 3, max = 20, message = "שם פרטי חייב להיות בין 3 ל-20 תווים")
    private String firstName;

    @NotBlank(message = "שם משפחה הוא שדה חובה")
    @Size(min = 3, max = 20, message = "שם משפחה חייב להיות בין 3 ל-20 תווים")
    private String lastName;

    @NotBlank(message = "סיסמה היא שדה חובה")
    @Size(min = 6, message = "הסיסמה חייבת להכיל לפחות 6 תווים")
    private String password;

    @NotBlank(message = "אימייל הוא שדה חובה")
    @Email(message = "אנא הכנס כתובת אימייל תקינה")
    private String email;

    private boolean commander;

    @NotNull(message = "חובה להעלות תעודת משרת מילואים!")
    @FileSize(message = "גודל הקובץ חייב להיות עד 1MB") // לא צריך להגדיר max, יקח מ-application.properties
    private MultipartFile militaryIdDoc;

    @NotBlank(message = "ספר על עצמך הוא שדה חובה")
    @Size(max = 500, message = "תיאור על עצמך לא יכול לעלות על 500 תווים")
    private String about;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

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

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
