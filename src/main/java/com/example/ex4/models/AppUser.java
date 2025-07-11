package com.example.ex4.models;

import jakarta.persistence.*;

@Entity
public class AppUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    private String password;

    private String email;

    @Lob
    @Column(/*name = "military_id_doc",*/ columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] militaryIdDoc;

    //@Column(name = "is_approved")
    private Boolean isApproved = false;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('ADMIN', 'COMMANDER', 'RESERVIST') DEFAULT 'RESERVIST'")
    private Role role = Role.RESERVIST;

    public Long getId() {
        return userId;
    }

    public void setId(Long id) {
        this.userId = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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
} 