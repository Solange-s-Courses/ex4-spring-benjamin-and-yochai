package com.example.ex4.models;

import jakarta.persistence.*;

@Entity
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobTitle;
    
    @Enumerated(EnumType.STRING)
    private LocationRegion location;
    
    private String assignmentType;
    
    private String description;
    
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('ACTIVE', 'CANCELED', 'FULFILLED', 'FROZEN') DEFAULT 'ACTIVE'")
    private PositionStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser publisher;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public AppUser getPublisher() {
        return publisher;
    }

    public void setPublisher(AppUser publisher) {
        this.publisher = publisher;
    }

    public PositionStatus getStatus() { return status; }

    public void setStatus(PositionStatus status) { this.status = status; }
}
