package com.example.ex4.dto;

import jakarta.validation.constraints.*;

public class InterviewForm {
    private Long applicationId;

    @NotBlank(message = "תאריך ראיון הוא שדה חובה")
    private String interviewDate;

    private String location;

    @Size(max = 1000, message = "הערות לא יכולות לעלות על 1000 תווים")
    private String notes;

    @NotNull(message = "חובה לציין אם הראיון וירטואלי")
    private Boolean isVirtual;

    /**
     * Default constructor
     */
    public InterviewForm() {}

    /**
     * Constructor with all parameters
     * 
     * @param applicationId Application ID
     * @param interviewDate Interview date
     * @param location Interview location
     * @param notes Interview notes
     * @param isVirtual Whether interview is virtual
     */
    public InterviewForm(Long applicationId, String interviewDate, String location, String notes, Boolean isVirtual) {
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.location = location;
        this.notes = notes;
        this.isVirtual = isVirtual;
    }

    /**
     * Gets the application ID
     * 
     * @return Application ID
     */
    public Long getApplicationId() { return applicationId; }
    
    /**
     * Sets the application ID
     * 
     * @param applicationId Application ID to set
     */
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    /**
     * Gets the interview date
     * 
     * @return Interview date
     */
    public String getInterviewDate() { return interviewDate; }
    
    /**
     * Sets the interview date
     * 
     * @param interviewDate Interview date to set
     */
    public void setInterviewDate(String interviewDate) { this.interviewDate = interviewDate; }

    /**
     * Gets the location
     * 
     * @return Interview location
     */
    public String getLocation() { return location; }
    
    /**
     * Sets the location
     * 
     * @param location Interview location to set
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Gets the notes
     * 
     * @return Interview notes
     */
    public String getNotes() { return notes; }
    
    /**
     * Sets the notes
     * 
     * @param notes Interview notes to set
     */
    public void setNotes(String notes) { this.notes = notes; }

    /**
     * Gets the virtual status
     * 
     * @return Whether interview is virtual
     */
    public Boolean getIsVirtual() { return isVirtual; }
    
    /**
     * Sets the virtual status
     * 
     * @param isVirtual Whether interview is virtual
     */
    public void setIsVirtual(Boolean isVirtual) { this.isVirtual = isVirtual; }

    /**
     * Returns string representation of the form
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return "InterviewForm{" +
                "applicationId=" + applicationId +
                ", interviewDate='" + interviewDate + '\'' +
                ", location='" + location + '\'' +
                ", notes='" + notes + '\'' +
                ", isVirtual=" + isVirtual +
                '}';
    }
} 