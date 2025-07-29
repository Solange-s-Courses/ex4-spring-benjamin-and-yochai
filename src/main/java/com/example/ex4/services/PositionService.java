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
    //@Autowired
    //private ApplicationService applicationService;

    public boolean existsByJobTitle(String jobTitle) {
        return positionRepository.existsByJobTitle(jobTitle);
    }

    public Position findById(Long id) {
        return positionRepository.findById(id).orElse(null);
    }

    public void save(Position position) {
        positionRepository.save(position);
    }

    public List<String> getAllDistinctJobTitles() {
        return positionRepository.findDistinctJobTitles();
    }

//    public String processRequirements(List<String> requirements) {
//        if (requirements == null || requirements.isEmpty()) {
//            return "";
//        }
//
//        return requirements.stream()
//                .filter(req -> req != null && !req.trim().isEmpty())
//                .collect(Collectors.joining(", "));
//    }

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

        // מיון אלפביתי של מיקומים - המרה ל-String
        if (!jobs.isEmpty()) {
            sortedLocationStrings = jobs.stream()
                    .map(Position::getLocation)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparing(LocationRegion::toString))
                    .map(LocationRegion::toString)
                    .toList();

            // מיון אלפביתי של סוגי שירות (Strings)
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

    public String processAddPositionForm(PositionForm form, Model model,
                                         BindingResult result, String username,
                                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //List<String> jobTitles = getAllDistinctJobTitles();
            //model.addAttribute("positionForm", form);
            //model.addAttribute("jobTitles", jobTitles);
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

    public List<Position> getPositionsByPublisher(String username) {
        AppUser user = appUserService.getUserByUsername(username);
        return positionRepository.findByPublisher(user);
    }

    public List<Map<String, Object>> getPositionsWithActiveApplicationCounts(String username) {
        AppUser user = appUserService.getUserByUsername(username);
        List<Position> positions = positionRepository.findByPublisher(user);

        return positions.stream().map(position -> {
            Map<String, Object> positionData = new HashMap<>();
            positionData.put("position", position);

            // Count active applications (PENDING status) for this position using repository directly
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

//    public List<PositionDto> searchPositions(String title, String location, String assignmentType, HttpSession session) {
//        // שמירת חיפוש אחרון רק אם יש מילת חיפוש טקסטואלית
//        if (title != null && !title.isBlank()) {
//            List<String> recentSearches = (List<String>) session.getAttribute("recent_searches");
//            if (recentSearches == null) recentSearches = new LinkedList<>();
//            recentSearches.remove(title);
//            recentSearches.add(0, title);
//            if (recentSearches.size() > 5) recentSearches = recentSearches.subList(0, 5);
//            session.setAttribute("recent_searches", recentSearches);
//        }
//
//        // החזרת התוצאות עם הפילטרים
//        return new LinkedList<>();
//        //return searchPositions(title, location, assignmentType);
//    }

//    public List<String> getRecentSearches(HttpSession session) {
//        List<String> recentSearches = (List<String>) session.getAttribute("recent_searches");
//        return recentSearches != null ? recentSearches : new LinkedList<>();
//    }

//    public List<PositionDto> searchPositions(String title, String location, String assignmentType) {
//        // כל הפילטרים נשלחו
//        if (location != null && !location.isEmpty() && assignmentType != null && !assignmentType.isEmpty()) {
//            return positionRepository.findByJobTitleContainingIgnoreCaseAndLocationAndAssignmentTypeAndStatus(
//                    title, LocationRegion.valueOf(location), assignmentType, PositionStatus.ACTIVE)
//                .stream()
//                .map(pos -> new PositionDto(
//                    pos.getId(),
//                    pos.getJobTitle(),
//                    pos.getLocation() != null ? pos.getLocation().name() : "",
//                    pos.getAssignmentType(),
//                    pos.getDescription(),
//                    pos.getRequirements(),
//                    pos.getPublisher() != null ?
//                        (pos.getPublisher().getFirstName() + " " + pos.getPublisher().getLastName()) : ""
//                ))
//                .collect(Collectors.toList());
//        }
//        // רק location
//        else if (location != null && !location.isEmpty()) {
//            return positionRepository.findByJobTitleContainingIgnoreCaseAndLocationAndStatus(
//                    title, LocationRegion.valueOf(location), PositionStatus.ACTIVE)
//                .stream()
//                .map(pos -> new PositionDto(
//                    pos.getId(),
//                    pos.getJobTitle(),
//                    pos.getLocation() != null ? pos.getLocation().name() : "",
//                    pos.getAssignmentType(),
//                    pos.getDescription(),
//                    pos.getRequirements(),
//                    pos.getPublisher() != null ?
//                        (pos.getPublisher().getFirstName() + " " + pos.getPublisher().getLastName()) : ""
//                ))
//                .collect(Collectors.toList());
//        }
//        // רק assignmentType
//        else if (assignmentType != null && !assignmentType.isEmpty()) {
//            return positionRepository.findByJobTitleContainingIgnoreCaseAndAssignmentTypeAndStatus(
//                    title, assignmentType, PositionStatus.ACTIVE)
//                .stream()
//                .map(pos -> new PositionDto(
//                    pos.getId(),
//                    pos.getJobTitle(),
//                    pos.getLocation() != null ? pos.getLocation().name() : "",
//                    pos.getAssignmentType(),
//                    pos.getDescription(),
//                    pos.getRequirements(),
//                    pos.getPublisher() != null ?
//                        (pos.getPublisher().getFirstName() + " " + pos.getPublisher().getLastName()) : ""
//                ))
//                .collect(Collectors.toList());
//        }
//        // רק חיפוש טקסטואלי
//        else {
//            return positionRepository.findByJobTitleContainingIgnoreCaseAndStatus(title, PositionStatus.ACTIVE)
//                .stream()
//                .map(pos -> new PositionDto(
//                    pos.getId(),
//                    pos.getJobTitle(),
//                    pos.getLocation() != null ? pos.getLocation().name() : "",
//                    pos.getAssignmentType(),
//                    pos.getDescription(),
//                    pos.getRequirements(),
//                    pos.getPublisher() != null ?
//                        (pos.getPublisher().getFirstName() + " " + pos.getPublisher().getLastName()) : ""
//                ))
//                .collect(Collectors.toList());
//        }
//    }

//    public List<PositionDto> getAllPositions() {
//        return positionRepository.findByStatus(PositionStatus.ACTIVE)
//            .stream()
//            .map(pos -> new PositionDto(
//                pos.getId(),
//                pos.getJobTitle(),
//                pos.getLocation() != null ? pos.getLocation().name() : "",
//                pos.getAssignmentType(),
//                pos.getDescription(),
//                pos.getRequirements(),
//                pos.getPublisher() != null ?
//                    (pos.getPublisher().getFirstName() + " " + pos.getPublisher().getLastName()) : ""
//            ))
//            .collect(Collectors.toList());
//    }

    /*@Transactional
    public ResponseEntity<Map<String, Object>> applyForPosition(Long id, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            applicationService.submitApplication(id, principal.getName());
            response.put("message", "המועמדות הוגשה בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בהגשת המועמדות.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }*/

    /*public String getEditPositionForm(Long id, Model model, String username) {
        Position position = findById(id);
        if (position == null || !position.getPublisher().getUsername().equals(username)) {
            throw new RuntimeException("אין לך הרשאה לערוך משרה זו");
        }
        com.example.ex4.dto.PositionForm form = new com.example.ex4.dto.PositionForm();
        form.setJobTitle(position.getJobTitle());
        form.setOtherJobTitle("");
        form.setLocation(position.getLocation());
        form.setAssignmentType(position.getAssignmentType());
        form.setDescription(position.getDescription());
        form.setRequirements(position.getRequirements() != null ? position.getRequirements().split(", ") : new String[]{});
        List<String> jobTitles = getAllDistinctJobTitles();
        model.addAttribute("positionForm", form);
        model.addAttribute("jobTitles", jobTitles);
        model.addAttribute("editMode", true);
        model.addAttribute("positionId", id);
        return "add-position";
    }*/

    /*public String processEditPositionForm(Long id, com.example.ex4.dto.PositionForm form, Model model,
                                          org.springframework.validation.BindingResult result, String username,
                                          org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Position position = findById(id);
        if (position == null || !position.getPublisher().getUsername().equals(username)) {
            throw new RuntimeException("אין לך הרשאה לערוך משרה זו");
        }
        if (result.hasErrors()) {
            List<String> jobTitles = getAllDistinctJobTitles();
            model.addAttribute("jobTitles", jobTitles);
            model.addAttribute("editMode", true);
            model.addAttribute("positionId", id);
            return "add-position";
        }
        if (form.getOtherJobTitle() != null && !form.getOtherJobTitle().isEmpty()) {
            position.setJobTitle(form.getOtherJobTitle());
        } else {
            position.setJobTitle(form.getJobTitle());
        }
        position.setLocation(form.getLocation());
        position.setAssignmentType(form.getAssignmentType());
        position.setDescription(form.getDescription());
        position.setRequirements(processRequirements(form.getRequirements() != null ? Arrays.asList(form.getRequirements()) : List.of()));
        save(position);
        redirectAttributes.addFlashAttribute("successMessage", "המשרה עודכנה בהצלחה!");
        return "redirect:/dashboard";
    }*/

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
