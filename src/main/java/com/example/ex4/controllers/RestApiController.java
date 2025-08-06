package com.example.ex4.controllers;

import com.example.ex4.models.*;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.PositionService;
import com.example.ex4.dto.InterviewForm;
import com.example.ex4.services.InterviewService;
import com.example.ex4.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;


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

    @GetMapping("/admin/document/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
        Optional<AppUser> userOpt = appUserService.getUserById(id);
        if (userOpt.isPresent()) {
            byte[] data = userOpt.get().getMilitaryIdDoc();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"military_id.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
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

    @GetMapping("/positions/active")
    public ResponseEntity<Map<String, Object>> getPositionsData(
            @RequestParam(value = "search", required = false) String searchTerm,
            HttpSession session) {
        return positionService.reloadPositions(searchTerm, session);
    }

    @GetMapping("/applications/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyForPosition(@PathVariable Long id,
                                   Principal principal) {
        return applicationService.submitApplication(id, principal.getName());
    }

    @GetMapping("/applications/{id}/cancel")
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
        return interviewService.scheduleInterviewApi(form);
    }

    @PostMapping("/interviews/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editInterview(@PathVariable Long id, @RequestBody InterviewForm form, Principal principal) {
        return interviewService.editInterviewApi(id, form, principal.getName());
    }

    @PostMapping("/interviews/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmInterview(@PathVariable Long id, Principal principal) {
        return interviewService.confirmInterviewApi(id, principal.getName());
    }
    
    @PostMapping("/interviews/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectInterview(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        return interviewService.rejectInterviewApi(id, body.get("reason"), principal.getName());
    }
    
    @PostMapping("/interviews/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeInterview(@PathVariable Long id, Principal principal) {
        return interviewService.completeInterviewApi(id, principal.getName());
    }
    
    @PostMapping("/interviews/{id}/summary")
    public ResponseEntity<Map<String, Object>> updateInterviewSummary(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        return interviewService.updateInterviewSummaryApi(id, body.get("summary"), principal.getName());
    }

    
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id, Principal principal) {
        return applicationService.approveApplicationApi(id, principal.getName());
    }
    
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectApplication(@PathVariable Long id, Principal principal) {
        return applicationService.rejectApplicationApi(id, principal.getName());
    }

    @GetMapping("/dashboard/poll")
    public ResponseEntity<Map<String, Object>> refreshDashboard(Principal principal) {
        AppUser user = appUserService.getUserByUsername(principal.getName());
        Map<String, Object> response = new HashMap<>();

        // Applications
        List<Application> submittedApplications = applicationService.getUserApplications(user.getUsername());
        response.put("myApplication", submittedApplications);

        // Interviews
        List<Interview> relevantInterviews;
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("COMMANDER")) {
            List<Interview> all = interviewService.getAllInterviews();
            relevantInterviews = all.stream()
                    .filter(i -> i.getApplication().getApplicant().getUsername().equals(user.getUsername())
                            || i.getApplication().getPosition().getPublisher().getUsername().equals(user.getUsername()))
                    .toList();
        } else {
            relevantInterviews = interviewService.getInterviewsByUser(user);
        }
        response.put("interviews", relevantInterviews);

        // Stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplicationsCount", submittedApplications.size());
        stats.put("pendingApplicationsCount", submittedApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .count());
        stats.put("upcomingInterviewCount", relevantInterviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED || i.getStatus() == InterviewStatus.CONFIRMED)
                .count());
        response.put("stats", stats);

        // Positions (with active application counts)
        List<Map<String, Object>> myPositions = positionService.getPositionsWithActiveApplicationCounts(user.getUsername());
        response.put("myPositions", myPositions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/positions/{positionId}/poll")
    public ResponseEntity<Map<String, Object>> pollPositionApplicants(@PathVariable Long positionId, Principal principal) {
        return applicationService.pollPositionApplicants(positionId, principal);
    }

    @GetMapping("/applications/{applicationId}/poll")
    public ResponseEntity<Map<String, Object>> pollApplicationCommander(@PathVariable Long applicationId, Principal principal) {
        return applicationService.pollApplicantsCommander(applicationId, principal);
    }
}

