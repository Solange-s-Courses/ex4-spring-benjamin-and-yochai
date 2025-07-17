package com.example.ex4.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

@Entity
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String username;

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String password;

    @NotEmpty
    private String email;

    @Lob
    @Column(/*name = "military_id_doc",*/ columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] militaryIdDoc;

    //@Column(name = "is_approved")
    private Boolean isApproved = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('ADMIN', 'COMMANDER', 'RESERVIST') DEFAULT 'RESERVIST'")
    private Role role = Role.RESERVIST;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('PENDING', 'APPROVED', 'BLOCKED') DEFAULT 'PENDING'")
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public byte[] getMilitaryIdDoc() {
        return militaryIdDoc;
    }

    public void setMilitaryIdDoc(byte[] militaryIdDoc) {
        this.militaryIdDoc = militaryIdDoc;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public RegistrationStatus getRegistrationStatus() { return registrationStatus; }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) { this.registrationStatus = registrationStatus; }
} 