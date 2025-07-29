package com.example.ex4.services;

import com.example.ex4.models.*;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.repositories.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public Interview scheduleInterview(Application application, java.time.LocalDateTime interviewDate, String location, String notes, Boolean isVirtual) {
        // בדיקת התנגשות מול כל הראיונות של המפקד
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
        
        return interviewRepository.save(interview);
    }
    
    private void validateInterviewDateTime(java.time.LocalDateTime interviewDate, Long applicationId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (interviewDate.isBefore(now)) {
            throw new IllegalArgumentException("לא ניתן לקבוע ראיון בזמן שחלף");
        }
        
        List<Interview> existingInterviews = interviewRepository.findByApplicationId(applicationId);
        for (Interview existingInterview : existingInterviews) {
            if (existingInterview.getStatus() != InterviewStatus.CANCELED && 
                existingInterview.getStatus() != InterviewStatus.REJECTED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("כבר קיים ראיון באותו זמן");
            }
        }
    }
    
    private void validateInterviewDateTimeForEdit(java.time.LocalDateTime interviewDate, Long applicationId, Long currentInterviewId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (interviewDate.isBefore(now)) {
            throw new IllegalArgumentException("לא ניתן לקבוע ראיון בזמן שחלף");
        }
        
        List<Interview> existingInterviews = interviewRepository.findByApplicationId(applicationId);
        for (Interview existingInterview : existingInterviews) {
            if (!existingInterview.getId().equals(currentInterviewId) &&
                existingInterview.getStatus() != InterviewStatus.CANCELED && 
                existingInterview.getStatus() != InterviewStatus.REJECTED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("כבר קיים ראיון באותו זמן");
            }
        }
    }

    private String generateJitsiLink(Interview interview) {
        String roomId = "interview-" + interview.getApplication().getPosition().getId() + 
                       "-" + interview.getApplication().getApplicant().getUsername() +
                       "-" + interview.getApplication().getPosition().getPublisher().getUsername();
        
        roomId = roomId.replaceAll("[^a-zA-Z0-9-]", "");
        
        return "https://meet.jit.si/" + roomId;
    }

    private void validateApplicantInterviewTime(java.time.LocalDateTime interviewDate, Long applicantId, Long excludeInterviewId) {
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

    public void confirmInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        
        validateApplicantInterviewTime(interview.getInterviewDate(), interview.getApplication().getApplicant().getId(), interviewId);
        
        interview.setStatus(InterviewStatus.CONFIRMED);
        interviewRepository.save(interview);
    }

    public void rejectInterview(Long interviewId, String reason) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        interview.setStatus(InterviewStatus.REJECTED);
        interview.setRejectionReason(reason);
        interviewRepository.save(interview);
    }

    public List<Interview> getInterviewsByApplication(Application application) {
        return interviewRepository.findByApplication(application);
    }

    public List<Interview> getInterviewsByUser(AppUser user) {
        List<Application> applications = applicationRepository.findByApplicant(user);
        List<Interview> interviews = new java.util.ArrayList<>();
        for (Application app : applications) {
            interviews.addAll(getInterviewsByApplication(app));
        }
        return interviews;
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id).orElse(null);
    }

    public Interview cancelInterview(Long id) {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.setStatus(InterviewStatus.CANCELED);
        interview.setInterviewSummary("מועמדות בוטלה");
        return interviewRepository.save(interview);
    }

    public Interview completeInterview(Long id) {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.setStatus(InterviewStatus.COMPLETED);
        return interviewRepository.save(interview);
    }

    public Interview saveInterview(Interview interview) {
        validateInterviewDateTimeForEdit(interview.getInterviewDate(), interview.getApplication().getId(), interview.getId());
        return interviewRepository.save(interview);
    }

    public void validateInterviewDateTimeForPosition(java.time.LocalDateTime interviewDate, Long positionId, Long excludeInterviewId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (interviewDate.isBefore(now)) {
            throw new IllegalArgumentException("לא ניתן לקבוע ראיון בזמן שחלף");
        }
        
        List<Interview> allInterviews = interviewRepository.findAll();
        for (Interview existingInterview : allInterviews) {
            if (excludeInterviewId != null && existingInterview.getId().equals(excludeInterviewId)) {
                continue; // דלג על הראיון הנוכחי
            }
            
            if (existingInterview.getApplication().getPosition().getId().equals(positionId) && // אותו תפקיד
                existingInterview.getStatus() != InterviewStatus.CANCELED && 
                existingInterview.getStatus() != InterviewStatus.REJECTED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("יש כבר ראיון שנקבע לזמן זה. אנא בחר זמן אחר.");
            }
        }
    }

    private void validateInterviewDateTimeForCommander(java.time.LocalDateTime interviewDate, Long commanderId, Long excludeInterviewId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        
        if (interviewDate.isBefore(now)) {
            throw new IllegalArgumentException("לא ניתן לקבוע ראיון בזמן שחלף");
        }
        
        List<Interview> allInterviews = interviewRepository.findAll();
        for (Interview existingInterview : allInterviews) {
            if (excludeInterviewId != null && existingInterview.getId().equals(excludeInterviewId)) {
                continue; // דלג על הראיון הנוכחי
            }
            
            // בדוק אם זה ראיון של אותו מפקד
            if (existingInterview.getApplication().getPosition().getPublisher().getId().equals(commanderId) && 
                existingInterview.getStatus() != InterviewStatus.CANCELED && 
                existingInterview.getStatus() != InterviewStatus.REJECTED &&
                existingInterview.getInterviewDate().equals(interviewDate)) {
                throw new IllegalArgumentException("יש כבר ראיון שנקבע לזמן זה. אנא בחר זמן אחר.");
            }
        }
    }

    public Interview updateInterview(Long interviewId, java.time.LocalDateTime newDate, String location, String notes, Boolean isVirtual) {
        Interview interview = getInterviewById(interviewId);
        if (interview == null) {
            throw new IllegalArgumentException("הראיון לא נמצא");
        }
        
        boolean dateChanged = !interview.getInterviewDate().equals(newDate);
        
        // בדיקת התנגשות ראיונות אם התאריך השתנה
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
        
        return interviewRepository.save(interview);
    }

    public String getUpdateMessage(Interview originalInterview, java.time.LocalDateTime newDate) {
        boolean dateChanged = !originalInterview.getInterviewDate().equals(newDate);
        return dateChanged ? 
            "הראיון עודכן בהצלחה. הסטטוס שונה ל'ממתין לאישור' עקב שינוי בתאריך/שעה." :
            "הראיון עודכן בהצלחה.";
    }

    public java.util.List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    public ResponseEntity<Map<String, Object>> confirmInterviewApi(Long interviewId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
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
    
    public ResponseEntity<Map<String, Object>> rejectInterviewApi(Long interviewId, String reason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            rejectInterview(interviewId, reason);
            response.put("message", "הראיון נדחה בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בדחיית הראיון.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 