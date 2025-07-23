package com.example.ex4.controllers;

import com.example.ex4.models.*;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.PositionService;
import com.example.ex4.repositories.PositionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private AppUserService appUserService;
    
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private com.example.ex4.services.InterviewService interviewService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {  return "about"; }

    @GetMapping("/login")
    public String login() {  return "login"; }

    @GetMapping("/dashboard")
    public String getDashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        AppUser user = appUserService.getUserByUsername(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        List<Application> submittedApplications = applicationService.getUserApplications(principal.getName());
        List<com.example.ex4.models.Interview> relevantInterviews;
        if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("COMMANDER")) {
            List<com.example.ex4.models.Interview> all = interviewService.getAllInterviews();
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
                .filter(i -> i.getStatus() == com.example.ex4.models.InterviewStatus.SCHEDULED || i.getStatus() == com.example.ex4.models.InterviewStatus.CONFIRMED)
                .count();
        model.addAttribute("applications", submittedApplications);
        model.addAttribute("relevantInterviews", relevantInterviews);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("upcomingInterviewCount", upcomingInterviewCount);
        model.addAttribute("myPositions", positionService.getPositionsByPublisher(principal.getName()));
        return "dashboard";
    }


}
