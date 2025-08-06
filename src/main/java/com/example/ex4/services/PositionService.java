package com.example.ex4.services;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.*;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.repositories.InterviewRepository;
import com.example.ex4.models.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
//import com.example.ex4.dto.PositionDto;
import jakarta.servlet.http.HttpSession;
import com.example.ex4.repositories.ApplicationRepository;

@Service
public class PositionService {
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private InterviewRepository interviewRepository;


    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(Position position) {
        positionRepository.save(position);
    }

    public List<String> getAllDistinctJobTitles() {
        return positionRepository.findDistinctJobTitles();
    }


    private List<Position> searchPositionsByStatus(String searchTerm, PositionStatus status) {
        List<Position> jobs;
        if (searchTerm == null || searchTerm.isBlank()){
            jobs = positionRepository.findByStatusOrderByJobTitleAsc(status);
        }else{
            jobs = positionRepository.findByJobTitleContainingIgnoreCaseAndStatusOrderByJobTitleAsc(searchTerm, status);
        }
        return jobs;
    }

    private Map<String, Object> getActivePositionsWithStringFilters(String searchTerm) {
        List<Position> jobs = searchPositionsByStatus(searchTerm, PositionStatus.ACTIVE);

        List<String> sortedLocationStrings = new ArrayList<>();
        List<String> sortedServiceTypes = new ArrayList<>();

        if (!jobs.isEmpty()) {
            sortedLocationStrings = jobs.stream()
                    .map(Position::getLocation)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparing(LocationRegion::toString))
                    .map(LocationRegion::toString)
                    .toList();

            sortedServiceTypes = jobs.stream()
                    .map(Position::getAssignmentType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();

        }
        Map<String, Object> result = new HashMap<>();
        result.put("jobs", jobs);
        result.put("locations", sortedLocationStrings);
        result.put("serviceTypes", sortedServiceTypes);

        return result;
    }

    public String getPositionsPage(Model model, HttpSession session) {
        Map<String, Object> result = getActivePositionsWithStringFilters("");

        model.addAttribute("jobs", result.get("jobs"));
        model.addAttribute("locations", result.get("locations"));
        model.addAttribute("serviceTypes", result.get("serviceTypes"));
        model.addAttribute("recentSearches", handleRecentSearches(null, session));

        return "positions-page";
    }

    public String getPositionPage(Long id, Model model, HttpServletRequest request) {
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

    @Transactional
    public String processAddPositionForm(PositionForm form, Model model,
                                         BindingResult result, String username,
                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-position";
        }

        AppUser publisher = appUserService.getUserByUsername(username);
        Position position = new Position(form, publisher);

        try{
            save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה הוספה בהצלחה!");
            return "redirect:/positions";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }

    public ResponseEntity<Map<String, Object>> reloadPositions(String searchTerm, HttpSession session) {
         try {
            Map<String, Object> response = getActivePositionsWithStringFilters(searchTerm);

            response.put("recentSearches", handleRecentSearches(searchTerm, session));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    public List<Map<String, Object>> getPositionsWithActiveApplicationCounts(String username) {
        AppUser user = appUserService.getUserByUsername(username);
        List<Position> positions = positionRepository.findByPublisher(user);

        return positions.stream().map(position -> {
            Map<String, Object> positionData = new HashMap<>();
            positionData.put("position", position);

            long activeApplications = applicationRepository.findByPosition(position).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .count();

            positionData.put("activeApplications", activeApplications);
            return positionData;
        }).collect(Collectors.toList());
    }

    private List<String> handleRecentSearches(String searchTerm, HttpSession session) {
        List<String> recentSearches = (List<String>) session.getAttribute("recent_searches");
        if (recentSearches == null) {
            recentSearches = new LinkedList<>();
        }
        if (searchTerm != null && !searchTerm.isBlank()){
            recentSearches.remove(searchTerm.trim());
            recentSearches.add(0, searchTerm.trim());
            if (recentSearches.size() > 5) {
                recentSearches = recentSearches.subList(0, 5);
            }
            session.setAttribute("recent_searches", recentSearches);
        }

        return recentSearches;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> changePositionStatus(Long id, String status, String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Position position = findById(id);
            if (position == null || !position.getPublisher().getUsername().equals(username)) {
                response.put("message", "שגיאה בעדכון סטטוס המשרה.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            position.setStatus(Enum.valueOf(PositionStatus.class, status));
            save(position);

            List<Application>applications = applicationRepository.getApplicationsByPosition(position);
            for (Application application : applications) {
                if (application.getStatus() == ApplicationStatus.PENDING) {
                    application.setStatus(ApplicationStatus.CANCELED);

                    List<Interview>interviews = interviewRepository.findByApplication(application);
                    for (Interview interview : interviews) {
                        if (interview.getStatus() == InterviewStatus.SCHEDULED ||
                            interview.getStatus() == InterviewStatus.CONFIRMED)
                        {
                            interview.setStatus(InterviewStatus.CANCELED);
                            interview.setRejectionReason("המשרה לא רלוונטית");
                        }
                    }
                    interviewRepository.saveAll(interviews);
                }

            }
            applicationRepository.saveAll(applications);



            response.put("message", "סטטוס המשרה עודכן בהצלחה! כל המועמדויות בוטלו.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בעדכון סטטוס המשרה.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    public String getEditPosition(Long id, Model model, Principal principal) {
        Position position = findById(id);
        if (position == null || !position.getPublisher().getUsername().equals(principal.getName())) {
            return "error";
        }

        PositionForm form = new PositionForm(position);

        List<String> jobTitles = getAllDistinctJobTitles();

        model.addAttribute("positionForm", form);
        model.addAttribute("positionId", id);
        model.addAttribute("jobTitles", jobTitles);
        model.addAttribute("editMode", true);

        return "add-position";
    }

    @Transactional
    public String processEditPositionForm(Long id, @Valid PositionForm positionForm,
                                          Model model, BindingResult result, String username,
                                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-position";
        }

        AppUser publisher = appUserService.getUserByUsername(username);
        Position position = new Position(positionForm, publisher);

        position.setId(id);

        try{
            save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה עודכנה בהצלחה!");
            return "redirect:/positions/" + id;
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }
}
