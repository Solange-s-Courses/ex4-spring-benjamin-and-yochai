package com.example.ex4.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Application application;

    private LocalDateTime interviewDate;

    private String location;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    private String notes;

    private String rejectionReason;

    private String interviewSummary;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public String getInterviewSummary() { return interviewSummary; }
    public void setInterviewSummary(String interviewSummary) { this.interviewSummary = interviewSummary; }
} 