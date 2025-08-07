package com.example.ex4.services;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.*;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.repositories.InterviewRepository;
import com.example.ex4.repositories.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import com.example.ex4.repositories.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpSession;

@Service
public class PositionService {
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private InterviewRepository interviewRepository;

    /**
     * Finds a position by ID
     * 
     * @param id Position ID
     * @return Position object or null if not found
     */
    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }

    /**
     * Saves a position
     * 
     * @param position Position to save
     */
    @Transactional
    public void save(Position position) {
        positionRepository.save(position);
    }

    /**
     * Retrieves all distinct job titles
     * 
     * @return List of distinct job titles
     */
    public List<String> getAllDistinctJobTitles() {
        return positionRepository.findDistinctJobTitles();
    }

    /**
     * Searches positions by status with optional search term
     * 
     * @param searchTerm Optional search term
     * @param status Position status to filter by
     * @return List of positions matching criteria
     */
    private List<Position> searchPositionsByStatus(String searchTerm, PositionStatus status) {
        List<Position> jobs;
        if (searchTerm == null || searchTerm.isBlank()){
            jobs = positionRepository.findByStatusOrderByJobTitleAsc(status);
        }else{
            jobs = positionRepository.findByJobTitleContainingIgnoreCaseAndStatusOrderByJobTitleAsc(searchTerm, status);
        }
        return jobs;
    }

    /**
     * Gets active positions with string filters
     * 
     * @param searchTerm Optional search term
     * @return Map containing jobs, locations, and service types
     */
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

    /**
     * Gets the positions page data
     * 
     * @param model Spring MVC model
     * @param session HTTP session
     * @return Template name for positions page
     */
    public String getPositionsPage(Model model, HttpSession session) {
        Map<String, Object> result = getActivePositionsWithStringFilters("");

        model.addAttribute("jobs", result.get("jobs"));
        model.addAttribute("locations", result.get("locations"));
        model.addAttribute("serviceTypes", result.get("serviceTypes"));
        model.addAttribute("recentSearches", handleRecentSearches(null, session));

        return "positions-page";
    }

    /**
     * Gets a specific position page
     * 
     * @param id Position ID
     * @param model Spring MVC model
     * @return Template name for position page
     */
    public String getPositionPage(Long id, Model model) {
        Position position = positionRepository.findById(id).orElseThrow(() -> new RuntimeException("Position not found"));

        model.addAttribute("position", position);
        return "position";
    }

    /**
     * Gets the add position form
     * 
     * @param model Spring MVC model
     * @return Template name for add position form
     */
    public String getAddPositionForm(Model model) {
        PositionForm form = new PositionForm();
        form.setRequirements(Arrays.asList(""));

        List<String> jobTitles = getAllDistinctJobTitles();

        model.addAttribute("positionForm", form);
        model.addAttribute("jobTitles", jobTitles);

        return "add-position";
    }

    /**
     * Processes the add position form
     * 
     * @param form Position form data
     * @param result Binding result for validation
     * @param username Publisher username
     * @param redirectAttributes Redirect attributes for flash messages
     * @return Redirect URL or template name
     */
    @Transactional
    public String processAddPositionForm(PositionForm form,
                                         BindingResult result, String username,
                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-position";
        }

        AppUser publisher = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Position position = new Position(form, publisher);

        try{
            save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה הוספה בהצלחה!");
            return "redirect:/positions/" + position.getId();
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }

    /**
     * Reloads positions data
     * 
     * @param searchTerm Optional search term
     * @param session HTTP session
     * @return ResponseEntity containing positions data
     */
    public ResponseEntity<Map<String, Object>> reloadPositions(String searchTerm, HttpSession session) {
         try {
            Map<String, Object> response = getActivePositionsWithStringFilters(searchTerm);

            response.put("recentSearches", handleRecentSearches(searchTerm, session));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets positions with active application counts for a user
     * 
     * @param username Username to get positions for
     * @return List of maps containing position data and application counts
     */
    public List<Map<String, Object>> getPositionsWithActiveApplicationCounts(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    /**
     * Handles recent searches in session
     * 
     * @param searchTerm Search term to add
     * @param session HTTP session
     * @return List of recent searches
     */
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

    /**
     * Changes the status of a position
     * 
     * @param id Position ID
     * @param status New status
     * @param username Username of the position publisher
     * @return ResponseEntity containing status change result
     */
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

            if (!status.equals(PositionStatus.ACTIVE.name())){

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
                response.put("message", "סטטוס המשרה עודכן בהצלחה! כל המועמדויות והראיונות בוטלו.");
            }
            else{
                response.put("message", "סטטוס המשרה עודכן בהצלחה! המשרה כעת פעילה!");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בעדכון סטטוס המשרה.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Gets the edit position form
     * 
     * @param id Position ID
     * @param model Spring MVC model
     * @param principal Current authenticated user
     * @return Template name for edit position form
     */
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

    /**
     * Processes the edit position form
     * 
     * @param id Position ID
     * @param positionForm Position form data
     * @param result Binding result for validation
     * @param username Publisher username
     * @param redirectAttributes Redirect attributes for flash messages
     * @return Redirect URL or template name
     */
    @Transactional
    public String processEditPositionForm(Long id, @Valid PositionForm positionForm,
                                          BindingResult result, String username,
                                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "add-position";
        }

        AppUser publisher = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Position position = new Position(positionForm, publisher);

        position.setId(id);

        try{
            save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה עודכנה בהצלחה!");
            return "redirect:/positions/" + position.getId();

        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }
}
