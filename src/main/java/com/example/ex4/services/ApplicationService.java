package com.example.ex4.services;

import com.example.ex4.dto.ApplicationDto;
import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Position;
import com.example.ex4.models.Interview;
import com.example.ex4.models.InterviewStatus;
import com.example.ex4.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppUserService appUserService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private InterviewService interviewService;
    
    public boolean submitApplication(Long positionId, String username) {
        try {
            AppUser applicant = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (applicant == null || position == null) {
                return false;
            }
            
            Optional<Application> existingApplication = 
                applicationRepository.findByApplicantAndPosition(applicant, position);
            
            if (existingApplication.isPresent()) {
                Application existing = existingApplication.get();
                // אם המועמדות מבוטלת, אפשר להגיש מחדש
                if (existing.getStatus() == ApplicationStatus.CANCELED) {
                    existing.setStatus(ApplicationStatus.PENDING);
                    existing.setApplicationDate(LocalDateTime.now());
                    applicationRepository.save(existing);
                    return true;
                }
                return false;
            }
            
            Application application = new Application(applicant, position);
            applicationRepository.save(application);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isResubmission(Long positionId, String username) {
        try {
            AppUser applicant = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (applicant == null || position == null) {
                return false;
            }
            
            Optional<Application> existingApplication = 
                applicationRepository.findByApplicantAndPosition(applicant, position);
            
            return existingApplication.isPresent() && 
                   existingApplication.get().getStatus() == ApplicationStatus.CANCELED;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Application> getUserApplications(String username) {
        try {
            AppUser user = appUserService.getUserByUsername(username);
            List<Application> applications = applicationRepository.findByApplicant(user);
            return applications;//applications.stream()
                    //.map(this::convertToDto)
                    //.collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public List<ApplicationDto> getPositionApplications(Long positionId) {
        Position position = positionService.findById(positionId);
        if (position == null) {
            return new ArrayList<>();
        }
        
        List<Application> applications = applicationRepository.findByPosition(position);
        return applications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public boolean updateApplicationStatus(Long applicationId, ApplicationStatus status) {
        try {
            Optional<Application> applicationOpt = applicationRepository.findById(applicationId);
            if (applicationOpt.isPresent()) {
                Application application = applicationOpt.get();
                application.setStatus(status);
                applicationRepository.save(application);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasUserApplied(Long positionId, String username) {
        try {
            AppUser user = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (user == null || position == null) {
                return false;
            }
            
            Optional<Application> application = applicationRepository.findByApplicantAndPosition(user, position);
            
            return application.isPresent() && application.get().getStatus() != ApplicationStatus.CANCELED;
        } catch (Exception e) {
            return false;
        }
    }

    public ResponseEntity<Map<String, Object>> cancelApplication(Long positionId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AppUser user = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (user == null || position == null) {
                response.put("message", "שגיאה בביטול המועמדות.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Optional<Application> application = applicationRepository.findByApplicantAndPosition(user, position);
            
            if (application.isPresent()) {
                Application app = application.get();
                // רק מועמדויות במצב PENDING יכולות להיות מבוטלות
                if (app.getStatus() != ApplicationStatus.CANCELED) {
                    // ביטול המועמדות
                    app.setStatus(ApplicationStatus.CANCELED);
                    applicationRepository.save(app);
                    
                    // ביטול כל הראיונות של המועמדות הזאת
                    List<Interview> interviews = interviewService.getInterviewsByApplication(app);
                    for (Interview interview : interviews) {
                        if (interview.getStatus() != InterviewStatus.CANCELED) {
                            interviewService.cancelInterview(interview.getId());
                        }
                    }
                    
                    response.put("message", "המועמדות בוטלה בהצלחה! כל הראיונות בוטלו גם כן.");
                    return ResponseEntity.ok(response);
                }
            }
            
            response.put("message", "שגיאה בביטול המועמדות.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "שגיאה בביטול המועמדות.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    public Application getUserApplicationForPosition(Long positionId, String username) {
        try {
            AppUser user = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (user == null || position == null) {
                return null;
            }
            
            return applicationRepository.findByApplicantAndPosition(user, position).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Application> getApplicationsByPositionId(Long positionId) {
        try {
            Position position = positionService.findById(positionId);
            if (position == null) {
                return new ArrayList<>();
            }
            return applicationRepository.findByPosition(position);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private ApplicationDto convertToDto(Application application) {
        String location = "-";
        if (application.getPosition().getLocation() != null) {
            location = application.getPosition().getLocation().name();
        }
        
        String applicantName = application.getApplicant().getFirstName();
        applicantName = applicantName + " " + application.getApplicant().getLastName();
        
        return new ApplicationDto(
            application.getId(),
            application.getApplicant().getId(),
            applicantName,
            application.getPosition().getId(),
            application.getPosition().getJobTitle(),
            location,
            application.getPosition().getAssignmentType(),
            application.getApplicationDate(),
            application.getStatus()
        );
    }

    public Application getApplicationById(long id){
        return applicationRepository.findById(id).orElse(null);
    }
} 