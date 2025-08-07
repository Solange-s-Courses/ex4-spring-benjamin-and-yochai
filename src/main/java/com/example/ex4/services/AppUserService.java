package com.example.ex4.services;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.models.Role;
import com.example.ex4.models.Position;
import com.example.ex4.models.PositionStatus;
import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.models.Interview;
import com.example.ex4.models.InterviewStatus;
import com.example.ex4.repositories.AppUserRepository;
import com.example.ex4.repositories.PositionRepository;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.repositories.InterviewRepository;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppUserService implements UserDetailsService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PositionRepository positionRepository;
    private final PositionService positionService;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    @Autowired
    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, 
                         PositionRepository positionRepository, PositionService positionService,
                         ApplicationRepository applicationRepository, InterviewRepository interviewRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.positionRepository = positionRepository;
        this.positionService = positionService;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
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

        if (status != RegistrationStatus.APPROVED) {
            freezeUserPositions(user);
            cancelUserApplications(user);
        }

        return ResponseEntity.ok(user);
    }

    /**
     * Freezes all active positions of a user
     * 
     * @param user The user whose positions should be frozen
     */
    private void freezeUserPositions(AppUser user) {
        List<Position> userPositions = positionRepository.findByPublisher(user);
        for (Position position : userPositions) {
            if (position.getStatus() == PositionStatus.ACTIVE) {
                positionService.changePositionStatus(position.getId(), PositionStatus.FROZEN.name(), user.getUsername());
            }
        }
    }

    /**
     * Cancels all pending applications of a user
     * 
     * @param user The user whose applications should be canceled
     */
    private void cancelUserApplications(AppUser user) {
        List<Application> userApplications = applicationRepository.findByApplicant(user);
        for (Application application : userApplications) {
            if (application.getStatus() == ApplicationStatus.PENDING) {

                application.setStatus(ApplicationStatus.CANCELED);
                applicationRepository.save(application);
                
                List<Interview> interviews = interviewRepository.findByApplication(application);
                for (Interview interview : interviews) {
                    if (interview.getStatus() == InterviewStatus.SCHEDULED ||
                        interview.getStatus() == InterviewStatus.CONFIRMED) {
                        interview.setStatus(InterviewStatus.CANCELED);
                        interview.setRejectionReason("המשתמש נחסם");
                    }
                }
                interviewRepository.saveAll(interviews);
            }
        }
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

    /**
     * Processes user registration
     *
     * @param form Registration form data
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes for flash messages
     * @return Redirect URL or template name
     */
    public String registerUser( RegistrationForm form, BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (form.getMilitaryIdDoc() == null || form.getMilitaryIdDoc().isEmpty()) {
            result.rejectValue("militaryIdDoc", "error.militaryIdDoc", "חובה להעלות תעודת משרת מילואים!");
        }
        else {
            String contentType = form.getMilitaryIdDoc().getContentType();
            String fileName = form.getMilitaryIdDoc().getOriginalFilename();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf") || fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                result.rejectValue("militaryIdDoc", "error.militaryIdDoc", "יש להעלות קובץ PDF בלבד");
            }
        }

        if (result.hasErrors()) {
            return "register";
        }

        if (existsByUsername(form.getUsername())) {
            result.rejectValue("username", "error.appUser", "שם המשתמש כבר קיים");
            return "register";
        }

        if (existsByEmail(form.getEmail())) {
            result.rejectValue("email", "error.appUser", "האימייל כבר קיים");
            return "register";
        }

        try{
            saveUser(form);
            redirectAttributes.addFlashAttribute("successMessage", "ההרשמה נקלטה בהצלחה! יש להמתין לקבלת אישור ממנהל המערכת.");
            return "redirect:/login";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך ההרשמה, אנא נסו שנית במועד מאוחר יותר.");
            return "redirect:/register";
        }
    }

}