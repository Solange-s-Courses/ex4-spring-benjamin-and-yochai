package com.example.ex4.dto;

import com.example.ex4.models.LocationRegion;
import jakarta.validation.constraints.*;

/**
 * DTO for job posting form.
 */
public class PositionForm {
    /** שם התפקיד */
    @NotBlank
    private String jobTitle;
    /** תפקיד אחר (אם נבחר "אחר") */
    private String otherJobTitle;
    /** מיקום בארץ */
    @NotNull
    private LocationRegion location;
    /** סוג שיבוץ */
    @NotBlank
    private String assignmentType;
    /** תיאור תפקיד */
    @NotBlank
    private String description;
    /** דרישות מינימום */
    @NotBlank
    private String requirements;

    // No-args constructor
    public PositionForm() {}

    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    public String getOtherJobTitle() {
        return otherJobTitle;
    }
    public void setOtherJobTitle(String otherJobTitle) {
        this.otherJobTitle = otherJobTitle;
    }
    public LocationRegion getLocation() {
        return location;
    }
    public void setLocation(LocationRegion location) {
        this.location = location;
    }
    public String getAssignmentType() {
        return assignmentType;
    }
    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getRequirements() {
        return requirements;
    }
    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}
