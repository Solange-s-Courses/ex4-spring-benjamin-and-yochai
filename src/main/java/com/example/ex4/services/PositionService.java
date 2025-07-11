package com.example.ex4.services;

import com.example.ex4.models.AppUser;
import org.springframework.ui.Model;
import com.example.ex4.models.LocationRegion;
import com.example.ex4.models.Position;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    public String processRequirements(String[] requirements) {
        if (requirements == null || requirements.length == 0) {
            return "";
        }
        
        return String.join(", ",
            Arrays.stream(requirements)
                .filter(req -> req != null && !req.trim().isEmpty())
                .toArray(String[]::new)
        );
    }

    public String getPositions(Model model) {
        List<Position> jobs = positionRepository.findAll();
        model.addAttribute("jobs", jobs);

        return "positions-page";
    }
}
