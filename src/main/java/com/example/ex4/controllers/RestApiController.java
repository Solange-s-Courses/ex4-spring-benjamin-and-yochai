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

    /**
     * Retrieves a user's military ID document
     * 
     * @param id User ID
     * @return ResponseEntity containing the document or 404 if not found
     */
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

    /**
     * Retrieves all users
     * 
     * @return ResponseEntity containing list of all users
     */
    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    /**
     * Changes a user's registration status
     * 
     * @param body Request body containing userId and status
     * @return ResponseEntity containing the updated user
     */
    @PostMapping("/admin/changeUserStatus")
    public ResponseEntity<AppUser> changeUserStatus(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newStatus = body.get("status");

        return appUserService.changeUserStatus(userId, RegistrationStatus.valueOf(newStatus));
    }

    /**
     * Changes a user's role
     * 
     * @param body Request body containing userId and role
     * @return ResponseEntity containing the updated user
     */
    @PostMapping("/admin/changeUserRole")
    public ResponseEntity<AppUser> changeUserRole(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newRole = body.get("role");

        return appUserService.changeUserRole(userId, Role.valueOf(newRole));
    }

    /**
     * Reloads positions data with optional search term
     * 
     * @param searchTerm Optional search term
     * @param session HTTP session
     * @return ResponseEntity containing positions data
     */
    @GetMapping("/positions/active")
    public ResponseEntity<Map<String, Object>> getPositionsData(
            @RequestParam(value = "search", required = false) String searchTerm,
            HttpSession session) {
        return positionService.reloadPositions(searchTerm, session);
    }

    /**
     * Submits an application for a position
     * 
     * @param id Position ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing application result
     */
    @PostMapping("/applications/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyForPosition(@PathVariable Long id,
                                   Principal principal) {
        return applicationService.submitApplication(id, principal.getName());
    }

    /**
     * Cancels an application for a position
     * 
     * @param id Position ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing cancellation result
     */
    @PostMapping("/applications/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Long id,
                                Principal principal) {
        return applicationService.cancelApplication(id, principal.getName());
    }

    /**
     * Changes the status of a position
     * 
     * @param id Position ID
     * @param body Request body containing status
     * @param principal Current authenticated user
     * @return ResponseEntity containing status change result
     */
    @PutMapping("/positions/{id}/status")
    public ResponseEntity<Map<String, Object>> changePositionStatus(@PathVariable Long id,
                                                                   @RequestBody Map<String, String> body,
                                                                   Principal principal) {
        String status = body.get("status");
        return positionService.changePositionStatus(id, status, principal.getName());
    }

    /**
     * Schedules an interview
     * 
     * @param form Interview form data
     * @return ResponseEntity containing scheduling result
     */
    @PostMapping("/interviews/schedule")
    @ResponseBody
    public ResponseEntity<?> scheduleInterview(@RequestBody InterviewForm form) {
        return interviewService.scheduleInterviewApi(form);
    }

    /**
     * Edits an existing interview
     * 
     * @param id Interview ID
     * @param form Interview form data
     * @param principal Current authenticated user
     * @return ResponseEntity containing edit result
     */
    @PostMapping("/interviews/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editInterview(@PathVariable Long id, @RequestBody InterviewForm form, Principal principal) {
        return interviewService.editInterviewApi(id, form, principal.getName());
    }

    /**
     * Confirms an interview
     * 
     * @param id Interview ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing confirmation result
     */
    @PostMapping("/interviews/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmInterview(@PathVariable Long id, Principal principal) {
        return interviewService.confirmInterviewApi(id, principal.getName());
    }
    
    /**
     * Rejects an interview
     * 
     * @param id Interview ID
     * @param body Request body containing rejection reason
     * @param principal Current authenticated user
     * @return ResponseEntity containing rejection result
     */
    @PostMapping("/interviews/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectInterview(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        return interviewService.rejectInterviewApi(id, body.get("reason"), principal.getName());
    }
    
    /**
     * Completes an interview
     * 
     * @param id Interview ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing completion result
     */
    @PostMapping("/interviews/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeInterview(@PathVariable Long id, Principal principal) {
        return interviewService.completeInterviewApi(id, principal.getName());
    }
    
    /**
     * Updates interview summary
     * 
     * @param id Interview ID
     * @param body Request body containing summary
     * @param principal Current authenticated user
     * @return ResponseEntity containing update result
     */
    @PostMapping("/interviews/{id}/summary")
    public ResponseEntity<Map<String, Object>> updateInterviewSummary(@PathVariable Long id, @RequestBody Map<String, String> body, Principal principal) {
        return interviewService.updateInterviewSummaryApi(id, body.get("summary"), principal.getName());
    }

    
    /**
     * Approves an application
     * 
     * @param id Application ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing approval result
     */
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveApplication(@PathVariable Long id, Principal principal) {
        return applicationService.approveApplicationApi(id, principal.getName());
    }
    
    /**
     * Rejects an application
     * 
     * @param id Application ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing rejection result
     */
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectApplication(@PathVariable Long id, Principal principal) {
        return applicationService.rejectApplicationApi(id, principal.getName());
    }

    /**
     * Refreshes dashboard data
     * 
     * @param principal Current authenticated user
     * @return ResponseEntity containing dashboard data
     */
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

    /**
     * Polls for position applicants updates
     * 
     * @param positionId Position ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing applicants data
     */
    @GetMapping("/positions/{positionId}/poll")
    public ResponseEntity<Map<String, Object>> pollPositionApplicants(@PathVariable Long positionId, Principal principal) {
        return applicationService.pollPositionApplicants(positionId, principal);
    }

    /**
     * Polls for application commander updates
     * 
     * @param applicationId Application ID
     * @param principal Current authenticated user
     * @return ResponseEntity containing application data
     */
    @GetMapping("/applications/{applicationId}/poll")
    public ResponseEntity<Map<String, Object>> pollApplicationCommander(@PathVariable Long applicationId, Principal principal) {
        return applicationService.pollApplicantsCommander(applicationId, principal);
    }

    @PostMapping("/interviews/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelInterviews(@PathVariable Long id, Principal principal) {
        return interviewService.cancelInterviewApi(id, principal.getName());
    }

}

