package com.example.ex4.models;

public class Position {
    private String jobTitle;
    private LocationRegion location;
    private String assignmentType;
    private String description;
    private String requirements;

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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
