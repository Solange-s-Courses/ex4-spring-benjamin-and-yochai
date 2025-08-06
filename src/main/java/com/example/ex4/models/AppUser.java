package com.example.ex4.models;

import com.example.ex4.dto.RegistrationForm;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;

@Entity
@Table(name = "app_user")
public class AppUser implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] militaryIdDoc;

    @NotBlank
    @Column(name = "about", length = 500)
    private String about;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('ADMIN', 'COMMANDER', 'RESERVIST') DEFAULT 'RESERVIST'")
    private Role role = Role.RESERVIST;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('PENDING', 'APPROVED', 'BLOCKED') DEFAULT 'PENDING'")
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    public AppUser() {}

    public AppUser(RegistrationForm form) throws IOException {
        this.username = form.getUsername();
        this.firstName = form.getFirstName();
        this.lastName = form.getLastName();
        this.password = form.getPassword();
        this.email = form.getEmail();
        this.about = form.getAbout();

        if (form.isCommander()) {
            this.role = Role.COMMANDER;
        } else {
            this.role = Role.RESERVIST;
        }

        MultipartFile file = form.getMilitaryIdDoc();
        if (file != null && !file.isEmpty()) {
            this.militaryIdDoc = file.getBytes();
        }

    }

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

    public RegistrationStatus getRegistrationStatus() { return registrationStatus; }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) { this.registrationStatus = registrationStatus; }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return registrationStatus != RegistrationStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return registrationStatus == RegistrationStatus.APPROVED;
    }

} 