package com.example.ex4.services;

import com.example.ex4.models.*;
import com.example.ex4.repositories.InterviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InterviewService {
    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private com.example.ex4.repositories.ApplicationRepository applicationRepository;

    public Interview scheduleInterview(Application application, java.time.LocalDateTime interviewDate, String location, String notes) {
        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setInterviewDate(interviewDate);
        interview.setLocation(location);
        interview.setNotes(notes);
        interview.setStatus(InterviewStatus.SCHEDULED);
        return interviewRepository.save(interview);
    }

    public Interview confirmInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        interview.setStatus(InterviewStatus.CONFIRMED);
        return interviewRepository.save(interview);
    }

    public Interview rejectInterview(Long interviewId, String reason) {
        Interview interview = interviewRepository.findById(interviewId).orElseThrow();
        interview.setStatus(InterviewStatus.REJECTED);
        interview.setRejectionReason(reason);
        return interviewRepository.save(interview);
    }

    public List<Interview> getInterviewsByApplication(Application application) {
        return interviewRepository.findByApplication(application);
    }

    public List<Interview> getInterviewsByUser(AppUser user) {
        // Get all applications for this user from repository
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
        return interviewRepository.save(interview);
    }

    public Interview completeInterview(Long id) {
        Interview interview = interviewRepository.findById(id).orElseThrow();
        interview.setStatus(InterviewStatus.COMPLETED);
        return interviewRepository.save(interview);
    }

    public Interview saveInterview(Interview interview) {
        return interviewRepository.save(interview);
    }

    public java.util.List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }
} 