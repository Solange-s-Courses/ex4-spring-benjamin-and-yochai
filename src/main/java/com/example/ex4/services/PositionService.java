package com.example.ex4.services;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.PositionStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import com.example.ex4.models.LocationRegion;
import com.example.ex4.models.Position;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PositionService {
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AppUserService appUserService;

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

    private List<Position> getPositionsByStatus(PositionStatus status) {
        List<Position> jobs = positionRepository.findByStatusOrderByJobTitleAsc(status);
        return jobs;
    }

    private Map<String, Object> getActivePositionsWithStringFilters() {
        List<Position> jobs = getPositionsByStatus(PositionStatus.ACTIVE);

        // מיון אלפביתי של מיקומים - המרה ל-String
        List<String> sortedLocationStrings = jobs.stream()
                .map(Position::getLocation)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(LocationRegion::toString))
                .map(LocationRegion::toString)
                .toList();

        // מיון אלפביתי של סוגי שירות (Strings)
        List<String> sortedServiceTypes = jobs.stream()
                .map(Position::getAssignmentType)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("locations", sortedLocationStrings);
        result.put("serviceTypes", sortedServiceTypes);

        return result;
    }

    public String getPositionsPage(Model model) {
        Map<String, Object> result = getActivePositionsWithStringFilters();

        model.addAttribute("jobs", result.get("jobs"));
        model.addAttribute("locations", result.get("locations"));
        model.addAttribute("serviceTypes", result.get("serviceTypes"));

        return "positions-page";
    }

    public String getPosition(Long id, Model model) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new RuntimeException("Position not found"));
        model.addAttribute("position", position);
        return "position";
    }

    public String getAddPositionForm(Model model) {
        PositionForm form = new PositionForm();
        form.setRequirements(Arrays.asList(""));

        List<String> jobTitles = getAllDistinctJobTitles();

        model.addAttribute("positionForm", form);
        model.addAttribute("jobTitles", jobTitles);

        return "add-position";
    }

    public String processAddPositionForm(PositionForm form, Model model,
                                         BindingResult result, String username,
                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //List<String> jobTitles = getAllDistinctJobTitles();
            //model.addAttribute("positionForm", form);
            //model.addAttribute("jobTitles", jobTitles);
            return "add-position";
        }

        Position position = new Position();
        // Handle jobTitle/otherJobTitle logic
        if (StringUtils.hasText(form.getOtherJobTitle())) {
            position.setJobTitle(form.getOtherJobTitle());
        } else {
            position.setJobTitle(form.getJobTitle());
        }
        position.setLocation(form.getLocation());
        position.setAssignmentType(form.getAssignmentType());
        position.setDescription(form.getDescription());

        String processedRequirements = processRequirements(form.getRequirements());
        position.setRequirements(processedRequirements);

        AppUser publisher = appUserService.getUserByUsername(username);
        position.setPublisher(publisher);

        position.setStatus(PositionStatus.ACTIVE);

        try{
            save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה הוספה בהצלחה!");
            return "redirect:/positions";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }

    public ResponseEntity<Map<String, Object>> reloadPositions() {
         try {
            Map<String, Object> response = getActivePositionsWithStringFilters();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
