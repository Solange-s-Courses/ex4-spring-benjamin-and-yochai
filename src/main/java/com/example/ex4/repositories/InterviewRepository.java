package com.example.ex4.repositories;

import com.example.ex4.models.Interview;
import com.example.ex4.models.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByApplication(Application application);
    // אפשר להוסיף חיפושים נוספים לפי צורך
} 