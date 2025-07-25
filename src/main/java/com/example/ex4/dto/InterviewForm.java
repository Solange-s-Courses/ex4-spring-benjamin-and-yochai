package com.example.ex4.dto;

public class InterviewForm {
    private Long applicationId;
    private String interviewDate; // ISO string, to be parsed to LocalDateTime
    private String location;
    private String notes;
    private Boolean isVirtual;

    public InterviewForm() {}

    public InterviewForm(Long applicationId, String interviewDate, String location, String notes, Boolean isVirtual) {
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.location = location;
        this.notes = notes;
        this.isVirtual = isVirtual;
    }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getInterviewDate() { return interviewDate; }
    public void setInterviewDate(String interviewDate) { this.interviewDate = interviewDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsVirtual() { return isVirtual; }
    public void setIsVirtual(Boolean isVirtual) { this.isVirtual = isVirtual; }

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