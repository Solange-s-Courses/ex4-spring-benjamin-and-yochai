package com.example.ex4.dto;

import com.example.ex4.models.LocationRegion;
import com.example.ex4.validation.OtherJobTitleValidator;
import jakarta.validation.constraints.*;
import com.example.ex4.validation.RequirementsValidator;

import java.util.ArrayList;
import java.util.List;


@OtherJobTitleValidator
public class PositionForm {
    @NotBlank(message = "חובה לבחור תפקיד")
    private String jobTitle;

    //@Size(min = 2, message = "שם התפקיד חייב להכיל לפחות 2 תווים")
    private String otherJobTitle;

    @NotNull(message = "חובה לבחור מיקום")
    private LocationRegion location;

    @NotBlank(message = "חובה לבחור סוג שיבוץ")
    private String assignmentType;

    @NotBlank(message = "חובה להזין תיאור תפקיד")
    @Size(min = 10, max = 500, message = "תיאור התפקיד חייב להכיל בין 10 ל-500 תווים")
    private String description;

    @RequirementsValidator
    private List<String> requirements = new ArrayList<>();

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

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
}
