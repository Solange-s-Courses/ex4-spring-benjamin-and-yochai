package com.example.ex4.services;

import com.example.ex4.models.Position;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

@Service
public class PositionService {
    @Autowired
    private PositionRepository positionRepository;

    public boolean existsByJobTitle(String jobTitle) {
        return positionRepository.existsByJobTitle(jobTitle);
    }

    public void save(Position position) {
        positionRepository.save(position);
    }

    public List<String> getAllDistinctJobTitles() {
        return positionRepository.findDistinctJobTitles();
    }

    // מתודה חדשה לטיפול בדרישות מינימום
    public String processRequirements(String[] requirements) {
        if (requirements == null || requirements.length == 0) {
            return "";
        }
        
        // מסנן ריקים ומחבר עם שורות חדשות
        return String.join("\n", 
            Arrays.stream(requirements)
                .filter(req -> req != null && !req.trim().isEmpty())
                .toArray(String[]::new)
        );
    }
}
