package com.example.ex4.services;

import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Position;
import com.example.ex4.models.Interview;
import com.example.ex4.models.InterviewStatus;
import com.example.ex4.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ResponseEntity<Map<String, Object>> submitApplication(Long positionId, String username) {
        Map<String, Object> response = new HashMap<>();
        try {
            AppUser applicant = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (applicant == null || position == null) {
                response.put("message", "אירעה שגיאה בהגשת המועמדות.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (applicant == position.getPublisher()){
                response.put("message", "לא ניתן להגיש מועמדות למשרה שלך.");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            Optional<Application> existingApplication =
                applicationRepository.findByApplicantAndPosition(applicant, position);
            
            if (existingApplication.isPresent()) {
                Application existing = existingApplication.get();
                if (existing.getStatus() == ApplicationStatus.CANCELED) {
                    existing.setStatus(ApplicationStatus.PENDING);
                    existing.setApplicationDate(LocalDateTime.now());
                    applicationRepository.save(existing);
                    response.put("message", "המועמדות הוגשה בהצלחה");
                    return ResponseEntity.ok(response);
                }
                response.put("message", "קיימת מועמדות למשרה זו.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Application application = new Application(applicant, position);
            applicationRepository.save(application);

            response.put("message", "המועמדות הוגשה בהצלחה");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בהגשת המועמדות.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public List<Application> getUserApplications(String username) {
        try {
            AppUser user = appUserService.getUserByUsername(username);
            List<Application> applications = applicationRepository.findByApplicant(user);
            return applications;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }


    @Transactional
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


    @Transactional
    public ResponseEntity<Map<String, Object>> cancelApplication(Long positionId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AppUser user = appUserService.getUserByUsername(username);
            Position position = positionService.findById(positionId);
            
            if (user == null || position == null) {
                response.put("message", "שגיאה בביטול המועמדות.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Optional<Application> application = applicationRepository.findByApplicantAndPosition(user, position);
            
            if (application.isPresent()) {
                Application app = application.get();

                if (app.getStatus() == ApplicationStatus.PENDING) {
                    app.setStatus(ApplicationStatus.CANCELED);
                    applicationRepository.save(app);
                    
                    List<Interview> interviews = interviewService.getInterviewsByApplication(app);
                    for (Interview interview : interviews) {
                        if (interview.getStatus() != InterviewStatus.CANCELED) {
                            interviewService.cancelInterview(interview); //causing error-----------------------
                        }
                    }
                    
                    response.put("message", "המועמדות בוטלה בהצלחה! כל הראיונות בוטלו גם כן.");
                    return ResponseEntity.ok(response);
                }
            }
            response.put("message", "שגיאה בביטול המועמדות.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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

    public Application getApplicationById(long id){
        return applicationRepository.findById(id).orElse(null);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> approveApplicationApi(Long applicationId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Application application = getApplicationById(applicationId);
            if (application == null) {
                response.put("message", "המועמדות לא נמצאה");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!application.getPosition().getPublisher().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה לאשר מועמדות זו");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            boolean success = updateApplicationStatus(applicationId, ApplicationStatus.APPROVED);
            if (success) {
                response.put("message", "המועמדות אושרה בהצלחה!");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "שגיאה באישור המועמדות.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "שגיאה באישור המועמדות.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> rejectApplicationApi(Long applicationId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Application application = getApplicationById(applicationId);
            if (application == null) {
                response.put("message", "המועמדות לא נמצאה");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!application.getPosition().getPublisher().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה לדחות מועמדות זו");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            boolean success = updateApplicationStatus(applicationId, ApplicationStatus.REJECTED);
            if (success) {
                List<Interview> interviews = interviewService.getInterviewsByApplication(application);
                for (Interview interview : interviews) {
                    if (interview.getStatus() != InterviewStatus.CANCELED) {
                        interviewService.cancelInterview(interview);
                    }
                }
                
                response.put("message", "המועמדות נדחתה בהצלחה! כל הראיונות בוטלו גם כן.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "שגיאה בדחיית המועמדות.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("message", "שגיאה בדחיית המועמדות.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> pollPositionApplicants(Long positionId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        Position position = positionService.findById(positionId);

        if (position == null) {
            response.put("message", "המועמדות לא נמצאה");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (!position.getPublisher().getUsername().equals(principal.getName())) {
            response.put("message", "אין הרשאה");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Application>applications = applicationRepository.findByPosition(position);
        response.put("applications", applications);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> pollApplicantsCommander(Long applicationId, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        Application application = applicationRepository.findById(applicationId).orElse(null);

        if (application == null) {
            response.put("message", "המועמדות לא נמצאה");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        if (!application.getPosition().getPublisher().getUsername().equals(principal.getName())) {
            response.put("message", "אין הרשאה");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Fetch interviews related to this application
        List<Interview> interviews = interviewService.findByApplication(application);

        response.put("application", application);
        response.put("interviews", interviews);

        return ResponseEntity.ok(response);
    }
}