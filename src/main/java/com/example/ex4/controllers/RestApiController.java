package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.models.Role;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.PositionService;
//import com.example.ex4.dto.PositionDto;
import com.example.ex4.dto.InterviewForm;
import com.example.ex4.services.InterviewService;
import com.example.ex4.repositories.ApplicationRepository;
import com.example.ex4.models.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.time.LocalDateTime;
import com.example.ex4.models.Interview;
import com.example.ex4.models.InterviewStatus;


@Controller
@RequestMapping("/restapi")
public class RestApiController {
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private InterviewService interviewService;
    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping("/admin/document/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
        Optional<AppUser> userOpt = appUserService.getUserById(id);
        if (userOpt.isPresent()) {
            byte[] data = userOpt.get().getMilitaryIdDoc();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"military_id.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF) // קובץ PDF בלבד
                    .body(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @PostMapping("/admin/changeUserStatus")
    public ResponseEntity<AppUser> changeUserStatus(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newStatus = body.get("status");

        return appUserService.changeUserStatus(userId, RegistrationStatus.valueOf(newStatus));
    }

    @PostMapping("/admin/changeUserRole")
    public ResponseEntity<AppUser> changeUserRole(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newRole = body.get("role");

        return appUserService.changeUserRole(userId, Role.valueOf(newRole));
    }

//    @GetMapping("/positions")
//    @ResponseBody
//    public ResponseEntity<List<PositionDto>> searchPositions(
//            @RequestParam(value = "search", required = false) String search,
//            @RequestParam(value = "location", required = false) String location,
//            @RequestParam(value = "serviceType", required = false) String assignmentType,
//            HttpSession session) {
//
//        List<PositionDto> positions = positionService.searchPositions(
//            search != null ? search : "",
//            location,
//            assignmentType,
//            session
//        );
//        return ResponseEntity.ok(positions);
//    }
//
//    @GetMapping("/positions/recent-searches")
//    @ResponseBody
//    public List<String> getRecentSearches(HttpSession session) {
//        return positionService.getRecentSearches(session);
//    }

    @GetMapping("/positions/active")
    public ResponseEntity<Map<String, Object>> getPositionsData(
            @RequestParam(value = "search", required = false) String searchTerm,
            HttpSession session) {
        return positionService.reloadPositions(searchTerm, session);
    }

    @GetMapping("/application/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyForPosition(@PathVariable Long id,
                                   Principal principal) {
        return applicationService.submitApplication(id, principal.getName());
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            applicationService.submitApplication(id, principal.getName());
//            response.put("message", "המועמדות הוגשה בהצלחה!");
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("message", "אירעה שגיאה בהגשת המועמדות.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
    }

    @GetMapping("/application/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Long id,
                                Principal principal) {
        return applicationService.cancelApplication(id, principal.getName());
    }

    @PutMapping("/positions/{id}/status")
    public ResponseEntity<Map<String, Object>> changePositionStatus(@PathVariable Long id,
                                                                   @RequestBody Map<String, String> body,
                                                                   Principal principal) {
        String status = body.get("status");
        return positionService.changePositionStatus(id, status, principal.getName());
    }

    @PostMapping("/interviews/schedule")
    @ResponseBody
    public ResponseEntity<?> scheduleInterview(@RequestBody InterviewForm form) {
        try {
            Application application = applicationRepository.findById(form.getApplicationId()).orElseThrow();
            interviewService.scheduleInterview(
                application,
                LocalDateTime.parse(form.getInterviewDate()),
                form.getLocation(),
                form.getNotes(),
                form.getIsVirtual()
            );
            return ResponseEntity.ok().body(Map.of("success", true, "message", "הראיון נקבע בהצלחה!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "אירעה שגיאה בקביעת הראיון. ודא שכל השדות תקינים ונסה שוב."));
        }
    }

    @PostMapping("/interviews/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editInterview(@PathVariable Long id, @RequestBody InterviewForm form) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Interview originalInterview = interviewService.getInterviewById(id);
            if (originalInterview == null) {
                response.put("success", false);
                response.put("message", "הראיון לא נמצא");
                return ResponseEntity.ok(response);
            }
            
            LocalDateTime newDate = LocalDateTime.parse(form.getInterviewDate());
            
            Interview updatedInterview = interviewService.updateInterview(id, newDate, form.getLocation(), form.getNotes(), form.getIsVirtual());
            
            String message = interviewService.getUpdateMessage(originalInterview, newDate);
            
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "אירעה שגיאה בעדכון הראיון: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/interviews/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmInterview(@PathVariable Long id) {
        return interviewService.confirmInterviewApi(id);
    }
    
    @PostMapping("/interviews/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectInterview(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return interviewService.rejectInterviewApi(id, body.get("reason"));
    }
    
    @PostMapping("/interviews/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeInterview(@PathVariable Long id) {
        return interviewService.completeInterviewApi(id);
    }
    
    @PostMapping("/interviews/{id}/summary")
    public ResponseEntity<Map<String, Object>> updateInterviewSummary(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return interviewService.updateInterviewSummaryApi(id, body.get("summary"));
    }
    
    @PostMapping("/interviews/{id}/change-decision")
    public ResponseEntity<Map<String, Object>> changeInterviewDecision(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return interviewService.changeInterviewDecisionApi(id, body.get("status"), body.get("reason"));
    }
    
}

