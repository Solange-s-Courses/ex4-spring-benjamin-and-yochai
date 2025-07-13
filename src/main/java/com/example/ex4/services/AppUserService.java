package com.example.ex4.services;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Role;
import com.example.ex4.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoderService passwordEncoderService;

    @Autowired
    public AppUserService(AppUserRepository appUserRepository, PasswordEncoderService passwordEncoderService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoderService = passwordEncoderService;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> getUserByUsername(String username) {
        return appUserRepository.findByUsername(username);
    }

    public Optional<AppUser> getUserByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return appUserRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmail(email);
    }

    @Transactional
    public void saveUser(RegistrationForm form) throws IOException {

        AppUser appUser = new AppUser();
        appUser.setUsername(form.getUsername().trim());
        appUser.setPassword(passwordEncoderService.encodePassword(form.getPassword()));
        appUser.setEmail(form.getEmail().trim());

        if (form.isCommander()) {
            appUser.setRole(Role.COMMANDER);
        } else {
            appUser.setRole(Role.RESERVIST);
        }

        MultipartFile file = form.getMilitaryIdDoc();
        if (file != null && !file.isEmpty()) {
            appUser.setMilitaryIdDoc(file.getBytes());
        }
        appUserRepository.save(appUser);
    }

    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }
} 