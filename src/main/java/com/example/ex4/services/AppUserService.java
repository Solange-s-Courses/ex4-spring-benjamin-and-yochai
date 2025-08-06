package com.example.ex4.services;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.models.Role;
import com.example.ex4.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads user details for Spring Security authentication
     * 
     * @param username Username to load
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     * @throws LockedException if account is blocked
     * @throws DisabledException if account is pending approval
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getRegistrationStatus() == RegistrationStatus.BLOCKED) {
            throw new LockedException("החשבון שלך חסום. אנא פנה לתמיכה.");
        }

        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            throw new DisabledException("החשבון שלך ממתין לאישור. אנא המתן עד שתאושר על ידי מנהל המערכת.");
        }

        return user;
    }

    /**
     * Retrieves all users
     * 
     * @return List of all users
     */
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    /**
     * Retrieves users with pending registration status
     * 
     * @return List of pending users
     */
    public List<AppUser> getPendingUsers() {
        return appUserRepository.findByRegistrationStatus(RegistrationStatus.PENDING);
    }

    /**
     * Retrieves a user by ID
     * 
     * @param id User ID
     * @return Optional containing the user if found
     */
    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    /**
     * Retrieves a user by username
     * 
     * @param username Username to search for
     * @return AppUser object
     * @throws UsernameNotFoundException if user not found
     */
    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Checks if a username exists
     * 
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return appUserRepository.existsByUsername(username);
    }

    /**
     * Checks if an email exists
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return appUserRepository.existsByEmail(email);
    }

    /**
     * Saves a new user from registration form
     * 
     * @param form Registration form data
     * @throws IOException if file processing fails
     */
    @Transactional
    public void saveUser(RegistrationForm form) throws IOException {
        form.setPassword(passwordEncoder.encode(form.getPassword()));
        AppUser appUser = new AppUser(form);

        appUserRepository.save(appUser);
    }

    /**
     * Changes a user's registration status
     * 
     * @param id User ID
     * @param status New registration status
     * @return ResponseEntity containing the updated user
     */
    @Transactional
    public ResponseEntity<AppUser> changeUserStatus(Long id, RegistrationStatus status) {
        Optional<AppUser> userOpt = getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        AppUser user = userOpt.get();
        user.setRegistrationStatus(status);
        appUserRepository.save(user);

        return ResponseEntity.ok(user);
    }

    /**
     * Changes a user's role
     * 
     * @param id User ID
     * @param role New role
     * @return ResponseEntity containing the updated user
     */
    @Transactional
    public ResponseEntity<AppUser> changeUserRole(Long id, Role role) {
        Optional<AppUser> userOpt = appUserRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        AppUser user = userOpt.get();
        user.setRole(role);
        appUserRepository.save(user);

        return ResponseEntity.ok(user);
    }

}