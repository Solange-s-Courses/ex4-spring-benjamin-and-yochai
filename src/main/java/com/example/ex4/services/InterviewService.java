package com.example.ex4.services;

import com.example.ex4.models.*;
import com.example.ex4.dto.InterviewForm;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.repositories.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@Service
public class InterviewService {
    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Schedules an interview
     * 
     * @param application Application for the interview
     * @param interviewDate Interview date and time
     * @param location Interview location
     * @param notes Interview notes
     * @param isVirtual Whether the interview is virtual
     */
    @Transactional
    public void scheduleInterview(Application application, LocalDateTime interviewDate, String location, String notes, Boolean isVirtual) {
        validateInterviewDateTimeForCommander(interviewDate, application.getPosition().getPublisher().getId(), null);
        
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewDate(interviewDate);
        interview.setLocation(location);
        interview.setNotes(notes);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setIsVirtual(isVirtual);
        
        if (isVirtual != null && isVirtual) {
            String jitsiLink = generateJitsiLink(interview);
            interview.setJitsiLink(jitsiLink);
        }

        interviewRepository.save(interview);
    }

    /**
     * Generates a Jitsi link for virtual interviews
     * 
     * @param interview Interview object
     * @return Jitsi meeting link
     */
    private String generateJitsiLink(Interview interview) {
        String roomId = "interview-" + interview.getApplication().getPosition().getId() + 
                       "-" + interview.getApplication().getApplicant().getUsername() +
                       "-" + interview.getApplication().getPosition().getPublisher().getUsername();
        
        roomId = roomId.replaceAll("[^a-zA-Z0-9-]", "");
        
        return "https://meet.jit.si/" + roomId;
    }

    /**
     * Validates applicant interview time conflicts
     * 
     * @param interviewDate Proposed interview date
     * @param applicantId Applicant ID
     * @param excludeInterviewId Interview ID to exclude from validation
     */
    private void validateApplicantInterviewTime(LocalDateTime interviewDate, Long applicantId, Long excludeInterviewId) {
        List<Interview> allInterviews = interviewRepository.findAll();
        for (Interview existingInterview : allInterviews) {
            if (excludeInterviewId != null && existingInterview.getId().equals(excludeInterviewId)) {
                continue;
            }
            
            if (existingInterview.getApplication().getApplicant().getId().equals(applicantId) && 
                existingInterview.getStatus() == InterviewStatus.CONFIRMED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("יש לך כבר ראיון מאושר באותו זמן. אנא דחה את הראיון הקיים קודם.");
            }
        }
    }

    /**
     * Confirms an interview
     * 
     * @param interviewId Interview ID
     */
    @Transactional
    public void confirmInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        
        validateApplicantInterviewTime(interview.getInterviewDate(), interview.getApplication().getApplicant().getId(), interviewId);
        
        interview.setStatus(InterviewStatus.CONFIRMED);
        interviewRepository.save(interview);
    }

    /**
     * Rejects an interview
     * 
     * @param interviewId Interview ID
     * @param reason Rejection reason
     */
    @Transactional
    public void rejectInterview(Long interviewId, String reason) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        interview.setStatus(InterviewStatus.REJECTED);
        interview.setRejectionReason(reason);
        interviewRepository.save(interview);
    }

    /**
     * Gets interviews for an application
     * 
     * @param application Application to get interviews for
     * @return List of interviews for the application
     */
    public List<Interview> getInterviewsByApplication(Application application) {
        return interviewRepository.findByApplication(application);
    }

    /**
     * Gets interviews for a user
     * 
     * @param user User to get interviews for
     * @return List of interviews for the user
     */
    public List<Interview> getInterviewsByUser(AppUser user) {
        List<Application> applications = applicationRepository.findByApplicant(user);
        List<Interview> interviews = new java.util.ArrayList<>();
        for (Application app : applications) {
            interviews.addAll(getInterviewsByApplication(app));
        }
        return interviews;
    }

    /**
     * Gets an interview by ID
     * 
     * @param id Interview ID
     * @return Interview object or null if not found
     */
    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id).orElse(null);
    }

    /**
     * Cancels an interview
     * 
     * @param interview Interview to cancel
     */
    @Transactional
    public void cancelInterview(Interview interview) {
        interview.setStatus(InterviewStatus.CANCELED);
        //interview.setInterviewSummary("מועמדות בוטלה");
        interviewRepository.save(interview);
    }

    /**
     * Completes an interview
     * 
     * @param id Interview ID
     */
    @Transactional
    public void completeInterview(Long id) {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.setStatus(InterviewStatus.COMPLETED);
        interviewRepository.save(interview);
    }

    /**
     * Validates interview date/time for commander
     * 
     * @param interviewDate Proposed interview date
     * @param commanderId Commander ID
     * @param excludeInterviewId Interview ID to exclude from validation
     */
    private void validateInterviewDateTimeForCommander(LocalDateTime interviewDate, Long commanderId, Long excludeInterviewId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (interviewDate.isBefore(now)) {
            throw new IllegalArgumentException("לא ניתן לקבוע ראיון בזמן שחלף");
        }
        
        List<Interview> allInterviews = interviewRepository.findAll();
        for (Interview existingInterview : allInterviews) {
            if (excludeInterviewId != null && existingInterview.getId().equals(excludeInterviewId)) {
                continue;
            }
            
            if (existingInterview.getApplication().getPosition().getPublisher().getId().equals(commanderId) &&
                existingInterview.getStatus() != InterviewStatus.CANCELED && 
                existingInterview.getStatus() != InterviewStatus.REJECTED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("יש כבר ראיון שנקבע לזמן זה. אנא בחר זמן אחר.");
            }
        }
    }

    /**
     * Updates an interview
     * 
     * @param interviewId Interview ID
     * @param newDate New interview date
     * @param location New location
     * @param notes New notes
     * @param isVirtual Whether the interview is virtual
     */
    @Transactional
    public void updateInterview(Long interviewId, LocalDateTime newDate, String location, String notes, Boolean isVirtual) {
        Interview interview = getInterviewById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("הראיון לא נמצא");
        }
        
        boolean dateChanged = !interview.getInterviewDate().equals(newDate);

        if (dateChanged) {
            validateInterviewDateTimeForCommander(newDate, interview.getApplication().getPosition().getPublisher().getId(), interviewId);
        }
        
        interview.setInterviewDate(newDate);
        interview.setLocation(location);
        interview.setNotes(notes);
        interview.setIsVirtual(isVirtual);
        
        if (dateChanged) {
            interview.setStatus(InterviewStatus.SCHEDULED);
        }
        
        interviewRepository.save(interview);
    }

    /**
     * Gets update message for interview changes
     * 
     * @param originalInterview Original interview object
     * @param newDate New interview date
     * @return Update message
     */
    public String getUpdateMessage(Interview originalInterview, LocalDateTime newDate) {
        boolean dateChanged = !originalInterview.getInterviewDate().equals(newDate);
        return dateChanged ? 
            "הראיון עודכן בהצלחה. הסטטוס שונה ל'ממתין לאישור' עקב שינוי בתאריך/שעה." :
            "הראיון עודכן בהצלחה.";
    }

    /**
     * Gets all interviews
     * 
     * @return List of all interviews
     */
    public java.util.List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    /**
     * Schedules an interview via API
     * 
     * @param form Interview form data
     * @return ResponseEntity containing scheduling result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> scheduleInterviewApi(InterviewForm form) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Application application = applicationRepository.findById(form.getApplicationId()).orElseThrow();
            scheduleInterview(
                application,
                LocalDateTime.parse(form.getInterviewDate()),
                form.getLocation(),
                form.getNotes(),
                form.getIsVirtual()
            );
            response.put("success", true);
            response.put("message", "הראיון נקבע בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "אירעה שגיאה בקביעת הראיון. ודא שכל השדות תקינים ונסה שוב.");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Edits an interview via API
     * 
     * @param id Interview ID
     * @param form Interview form data
     * @param username Username of the editor
     * @return ResponseEntity containing edit result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> editInterviewApi(Long id, InterviewForm form, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview originalInterview = getInterviewById(id);
            if (originalInterview == null) {
                response.put("success", false);
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!originalInterview.getApplication().getPosition().getPublisher().getUsername().equals(username)) {
                response.put("success", false);
                response.put("message", "אין לך הרשאה לערוך ראיון זה");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            Application application = originalInterview.getApplication();
            if (application.getStatus() == ApplicationStatus.REJECTED || application.getStatus() == ApplicationStatus.CANCELED) {
                response.put("success", false);
                response.put("message", "לא ניתן לערוך ראיון כאשר המועמדות נדחתה או בוטלה");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            LocalDateTime newDate = LocalDateTime.parse(form.getInterviewDate());
            
            updateInterview(id, newDate, form.getLocation(), form.getNotes(), form.getIsVirtual());
            
            String message = getUpdateMessage(originalInterview, newDate);
            
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "אירעה שגיאה בעדכון הראיון: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Confirms an interview via API
     * 
     * @param interviewId Interview ID
     * @param username Username of the confirmer
     * @return ResponseEntity containing confirmation result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmInterviewApi(Long interviewId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview interview = getInterviewById(interviewId);
            if (interview == null) {
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!interview.getApplication().getPosition().getPublisher().getUsername().equals(username) &&
                !interview.getApplication().getApplicant().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה לאשר ראיון זה");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            confirmInterview(interviewId);
            response.put("message", "הראיון אושר בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה באישור הראיון.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Rejects an interview via API
     * 
     * @param interviewId Interview ID
     * @param reason Rejection reason
     * @param username Username of the rejecter
     * @return ResponseEntity containing rejection result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> rejectInterviewApi(Long interviewId, String reason, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview interview = getInterviewById(interviewId);
            if (interview == null) {
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!interview.getApplication().getPosition().getPublisher().getUsername().equals(username) &&
                !interview.getApplication().getApplicant().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה לדחות ראיון זה");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            rejectInterview(interviewId, reason);
            response.put("message", "הראיון נדחה בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בדחיית הראיון.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Completes an interview via API
     * 
     * @param interviewId Interview ID
     * @param username Username of the completer
     * @return ResponseEntity containing completion result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> completeInterviewApi(Long interviewId, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview interview = getInterviewById(interviewId);
            if (interview == null) {
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!interview.getApplication().getPosition().getPublisher().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה להשלים ראיון זה");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            completeInterview(interviewId);
            response.put("message", "הראיון הושלם בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בהשלמת הראיון.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Updates interview summary via API
     * 
     * @param interviewId Interview ID
     * @param summary Interview summary
     * @param username Username of the updater
     * @return ResponseEntity containing update result
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> updateInterviewSummaryApi(Long interviewId, String summary, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview interview = getInterviewById(interviewId);
            if (interview == null) {
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!interview.getApplication().getPosition().getPublisher().getUsername().equals(username)) {
                response.put("message", "אין לך הרשאה לעדכן סיכום ראיון זה");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            interview.setInterviewSummary(summary);
            interviewRepository.save(interview);
            
            response.put("message", "סיכום הראיון עודכן בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בעדכון סיכום הראיון.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Finds interviews by application
     * 
     * @param application Application to find interviews for
     * @return List of interviews for the application
     */
    public List<Interview> findByApplication(Application application) {
        return interviewRepository.findByApplication(application);
    }
}