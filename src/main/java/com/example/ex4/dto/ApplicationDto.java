package com.example.ex4.dto;

import com.example.ex4.models.ApplicationStatus;
import java.time.LocalDateTime;

public class ApplicationDto {
    private Long id;
    private Long applicantId;
    private String applicantName;
    private Long positionId;
    private String positionTitle;
    private String positionLocation;
    private String positionAssignmentType;
    private LocalDateTime applicationDate;
    private ApplicationStatus status;

    public ApplicationDto() {}

    public ApplicationDto(Long id, Long applicantId, String applicantName, 
                         Long positionId, String positionTitle, 
                         String positionLocation, String positionAssignmentType,
                         LocalDateTime applicationDate, ApplicationStatus status) {
        this.id = id;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.positionLocation = positionLocation;
        this.positionAssignmentType = positionAssignmentType;
        this.applicationDate = applicationDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getPositionLocation() {
        return positionLocation;
    }

    public void setPositionLocation(String positionLocation) {
        this.positionLocation = positionLocation;
    }

    public String getPositionAssignmentType() {
        return positionAssignmentType;
    }

    public void setPositionAssignmentType(String positionAssignmentType) {
        this.positionAssignmentType = positionAssignmentType;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
} 