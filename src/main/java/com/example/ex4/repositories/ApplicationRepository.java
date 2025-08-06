package com.example.ex4.repositories;

import com.example.ex4.models.Application;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.models.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByApplicant(AppUser applicant);
    
    List<Application> findByPosition(Position position);
    
    Optional<Application> findByApplicantAndPosition(AppUser applicant, Position position);
    
    List<Application> findByStatus(ApplicationStatus status);

    Optional<Application> findByApplicantAndPositionAndStatus(AppUser user, Position position, ApplicationStatus applicationStatus);

    List<Application> getApplicationsByPosition(Position position);
}