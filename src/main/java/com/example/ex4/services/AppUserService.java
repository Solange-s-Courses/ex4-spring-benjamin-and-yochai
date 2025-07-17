/*package com.example.ex4.services;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Role;
import com.example.ex4.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        appUser.setFirstName(form.getFirstName().trim());
        appUser.setLastName(form.getLastName().trim());
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
*/
package com.example.ex4.services;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.models.Role;
import com.example.ex4.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
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

    // Spring Security UserDetailsService implementation
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

    // Your existing methods remain the same
    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public AppUser getUserByUsername(String username) {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
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
        appUser.setFirstName(form.getFirstName().trim());
        appUser.setLastName(form.getLastName().trim());
        appUser.setPassword(passwordEncoder.encode(form.getPassword()));
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

    @Transactional
    public void updateUser(AppUser user) {
        appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        appUserRepository.deleteById(id);
    }
}