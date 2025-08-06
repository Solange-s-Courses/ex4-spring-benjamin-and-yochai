package com.example.ex4.dto;

import com.example.ex4.models.LocationRegion;
import com.example.ex4.models.Position;
import com.example.ex4.validation.OtherJobTitleValidator;
import jakarta.validation.constraints.*;
import com.example.ex4.validation.RequirementsValidator;

import java.util.ArrayList;
import java.util.List;


@OtherJobTitleValidator
public class PositionForm {
    @NotBlank(message = "חובה לבחור תפקיד")
    private String jobTitle;

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

    public PositionForm(Position position){
        this.jobTitle = position.getJobTitle();
        this.location = position.getLocation();
        this.assignmentType = position.getAssignmentType();
        this.description = position.getDescription();
        this.requirements = List.of(position.getRequirements().split(", "));
    }

    /**
     * Gets the job title
     *
     * @return Job title
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * Sets the job title
     *
     * @param jobTitle Job title to set
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * Gets the other job title
     *
     * @return Other job title
     */
    public String getOtherJobTitle() {
        return otherJobTitle;
    }

    /**
     * Sets the other job title
     *
     * @param otherJobTitle Other job title to set
     */
    public void setOtherJobTitle(String otherJobTitle) {
        this.otherJobTitle = otherJobTitle;
    }

    /**
     * Gets the location
     *
     * @return Location
     */
    public LocationRegion getLocation() {
        return location;
    }

    /**
     * Sets the location
     *
     * @param location Location to set
     */
    public void setLocation(LocationRegion location) {
        this.location = location;
    }

    /**
     * Gets the assignment type
     *
     * @return Assignment type
     */
    public String getAssignmentType() {
        return assignmentType;
    }

    /**
     * Sets the assignment type
     *
     * @param assignmentType Assignment type to set
     */
    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    /**
     * Gets the description
     *
     * @return Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description Description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the requirements
     *
     * @return Requirements array
     */
    public List<String> getRequirements() {
        return requirements;
    }

    /**
     * Sets the requirements
     *
     * @param requirements Requirements array to set
     */
    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
}
