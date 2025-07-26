package com.example.ex4.models;

import com.example.ex4.dto.PositionForm;
import jakarta.persistence.*;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

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

    public Position() {}

    public Position(PositionForm positionForm, AppUser publisher) {
        if (StringUtils.hasText(positionForm.getOtherJobTitle())) {
            this.jobTitle = positionForm.getOtherJobTitle();
        } else {
            this.jobTitle = positionForm.getJobTitle();
        }

        this.location = positionForm.getLocation();
        this.assignmentType = positionForm.getAssignmentType();
        this.description = positionForm.getDescription();

        this.requirements = positionForm.getRequirements().stream()
                .filter(req -> req != null && !req.trim().isEmpty())
                .collect(Collectors.joining(", "));

        this.publisher = publisher;

        this.status = PositionStatus.ACTIVE;

    }

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
