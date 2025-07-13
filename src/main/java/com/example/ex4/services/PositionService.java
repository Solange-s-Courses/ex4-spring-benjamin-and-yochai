package com.example.ex4.services;

import com.example.ex4.models.AppUser;
import org.springframework.ui.Model;
import com.example.ex4.models.LocationRegion;
import com.example.ex4.models.Position;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

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

    public String processRequirements(List<String> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return "";
        }

        return requirements.stream()
                .filter(req -> req != null && !req.trim().isEmpty())
                .collect(Collectors.joining(", "));
    }

    public String getPositions(Model model) {
        List<Position> jobs = positionRepository.findAll();
        model.addAttribute("jobs", jobs);

        return "positions-page";
    }

    public String getPosition(@PathVariable Long id, Model model) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new RuntimeException("Position not found"));
        model.addAttribute("position", position);
        return "position";
    }
}
