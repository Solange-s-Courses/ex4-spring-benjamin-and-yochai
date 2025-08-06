package com.example.ex4.repositories;

import com.example.ex4.models.Position;
import com.example.ex4.models.PositionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    @Query("SELECT DISTINCT p.jobTitle FROM Position p")
    List<String> findDistinctJobTitles();

    boolean existsByJobTitle(String jobTitle);

    List<Position> findAllByOrderByJobTitleAsc();

    @Query("SELECT p FROM Position p WHERE p.status = :status AND p.publisher.registrationStatus = 'APPROVED' ORDER BY p.jobTitle ASC")
    List<Position> findByStatusOrderByJobTitleAsc(PositionStatus status);

    List<Position> findByPublisher(com.example.ex4.models.AppUser publisher);

    List<Position> findByJobTitleContainingIgnoreCase(String jobTitle);

    @Query("SELECT p FROM Position p WHERE p.jobTitle LIKE %:jobTitle% AND p.status = :status AND p.publisher.registrationStatus = 'APPROVED' ORDER BY p.jobTitle ASC")
    List<Position> findByJobTitleContainingIgnoreCaseAndStatusOrderByJobTitleAsc(String jobTitle, com.example.ex4.models.PositionStatus status);
    
    @Query("SELECT p FROM Position p WHERE p.status = :status AND p.publisher.registrationStatus = 'APPROVED'")
    List<Position> findByStatus(com.example.ex4.models.PositionStatus status);
}
