package com.example.ex4.dto;

import com.example.ex4.models.LocationRegion;
import jakarta.validation.constraints.*;


public class PositionForm {
    @NotBlank
    private String jobTitle;

    private String otherJobTitle;

    @NotNull
    private LocationRegion location;

    @NotBlank
    private String assignmentType;

    @NotBlank
    private String description;

    @NotBlank
    private String requirements;

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
