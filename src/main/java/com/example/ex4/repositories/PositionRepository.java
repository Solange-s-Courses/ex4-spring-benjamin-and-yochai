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

    List<Position> findByStatusOrderByJobTitleAsc(PositionStatus status);

    List<Position> findByPublisher(com.example.ex4.models.AppUser publisher);

    List<Position> findByJobTitleContainingIgnoreCase(String jobTitle);

    List<Position> findByJobTitleContainingIgnoreCaseAndLocationAndAssignmentTypeAndStatus(String jobTitle, com.example.ex4.models.LocationRegion location, String assignmentType, com.example.ex4.models.PositionStatus status);
    List<Position> findByJobTitleContainingIgnoreCaseAndLocationAndStatus(String jobTitle, com.example.ex4.models.LocationRegion location, com.example.ex4.models.PositionStatus status);
    List<Position> findByJobTitleContainingIgnoreCaseAndAssignmentTypeAndStatus(String jobTitle, String assignmentType, com.example.ex4.models.PositionStatus status);
    List<Position> findByJobTitleContainingIgnoreCaseAndStatus(String jobTitle, com.example.ex4.models.PositionStatus status);
    List<Position> findByStatus(com.example.ex4.models.PositionStatus status);
}
