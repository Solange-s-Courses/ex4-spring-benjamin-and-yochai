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
    @FileSize(message = "גודל הקובץ חייב להיות עד 1MB")
    private MultipartFile militaryIdDoc;

    @NotBlank(message = "ספר על עצמך הוא שדה חובה")
    @Size(max = 500, message = "תיאור על עצמך לא יכול לעלות על 500 תווים")
    private String about;

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

    /**
     * Gets the first name
     * 
     * @return First name
     */
    public String getFirstName() { return firstName; }

    /**
     * Sets the first name
     * 
     * @param firstName First name to set
     */
    public void setFirstName(String firstName) { this.firstName = firstName; }

    /**
     * Gets the last name
     * 
     * @return Last name
     */
    public String getLastName() { return lastName; }

    /**
     * Sets the last name
     * 
     * @param lastName Last name to set
     */
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Gets the password
     * 
     * @return Password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password
     * 
     * @param password Password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the email
     * 
     * @return Email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email
     * 
     * @param email Email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Checks if user is commander
     * 
     * @return true if commander, false otherwise
     */
    public boolean isCommander() {
        return commander;
    }

    /**
     * Sets commander status
     * 
     * @param commander Commander status to set
     */
    public void setCommander(boolean commander) {
        this.commander = commander;
    }

    /**
     * Gets the military ID document
     * 
     * @return Military ID document
     */
    public MultipartFile getMilitaryIdDoc() {
        return militaryIdDoc;
    }

    /**
     * Sets the military ID document
     * 
     * @param militaryIdDoc Military ID document to set
     */
    public void setMilitaryIdDoc(MultipartFile militaryIdDoc) {
        this.militaryIdDoc = militaryIdDoc;
    }

    /**
     * Gets the about text
     * 
     * @return About text
     */
    public String getAbout() {
        return about;
    }

    /**
     * Sets the about text
     * 
     * @param about About text to set
     */
    public void setAbout(String about) {
        this.about = about;
    }
}
