package com.example.ex4.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
public class Application {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id", nullable = false)
    private AppUser applicant;

    @ManyToOne
    @JoinColumn(name = "position_id", referencedColumnName = "id", nullable = false)
    private Position position;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING'")
    private ApplicationStatus status = ApplicationStatus.PENDING;

    public Application() {
        this.applicationDate = LocalDateTime.now();
    }

    public Application(AppUser applicant, Position position) {
        this();
        this.applicant = applicant;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getApplicant() {
        return applicant;
    }

    public void setApplicant(AppUser applicant) {
        this.applicant = applicant;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
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