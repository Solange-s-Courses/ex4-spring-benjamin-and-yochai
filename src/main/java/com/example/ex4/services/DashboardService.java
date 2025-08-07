package com.example.ex4.services;

import com.example.ex4.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private AppUserService appUserService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private InterviewService interviewService;

    /**
     * Displays the user dashboard with applications and interviews
     *
     * @param model Spring MVC model
     * @param principal Current authenticated user
     * @return The name of the dashboard template or redirect to login
     */
    public String getDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        AppUser user = appUserService.getUserByUsername(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        List<Application> submittedApplications = applicationService.getUserApplications(principal.getName());
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
        long totalApplications = submittedApplications.size();
        long pendingApplications = submittedApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                .count();
        long upcomingInterviewCount = relevantInterviews.stream()
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED || i.getStatus() == InterviewStatus.CONFIRMED)
                .count();
        model.addAttribute("applications", submittedApplications);
        model.addAttribute("relevantInterviews", relevantInterviews);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("upcomingInterviewCount", upcomingInterviewCount);
        model.addAttribute("myPositions", positionService.getPositionsWithActiveApplicationCounts(principal.getName()));
        return "dashboard";
    }


}
