package com.example.ex4.services;

import com.example.ex4.dto.ApplicationDto;
import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Position;
import com.example.ex4.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private AppUserService appUserService;
    
    @Autowired
    private PositionService positionService;

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
                return false;
            }
            
            Application application = new Application(applicant, position);
            applicationRepository.save(application);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<ApplicationDto> getUserApplications(String username) {
        try {
            AppUser user = appUserService.getUserByUsername(username);
            List<Application> applications = applicationRepository.findByApplicant(user);
            return applications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
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
            
            return applicationRepository.findByApplicantAndPosition(user, position).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    private ApplicationDto convertToDto(Application application) {
        return new ApplicationDto(
            application.getId(),
            application.getApplicant().getId(),
            application.getApplicant().getFirstName() + " " + application.getApplicant().getLastName(),
            application.getPosition().getId(),
            application.getPosition().getJobTitle(),
            application.getApplicationDate(),
            application.getStatus()
        );
    }
} 