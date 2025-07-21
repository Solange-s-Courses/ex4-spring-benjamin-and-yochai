package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.models.Application;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.PositionService;
import com.example.ex4.models.Position;
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
        
        // שליפת המשתמש האמיתי
        AppUser user = appUserService.getUserByUsername(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        // שליפת המועמדויות של המשתמש
        List<Application> submittedApplications = applicationService.getUserApplications(principal.getName());
        
        // סטטיסטיקות
        long totalApplications = submittedApplications.size();
        long pendingApplications = submittedApplications.stream()
                .filter(app -> app.getStatus() == com.example.ex4.models.ApplicationStatus.PENDING)
                .count();
        long upcomingInterviewCount = 0; // לא מומש עדיין

        // שליחה ל־Thymeleaf
        //model.addAttribute("user", user);
        model.addAttribute("applications", submittedApplications);
        model.addAttribute("upcomingInterviews", new ArrayList<>());
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("upcomingInterviewCount", upcomingInterviewCount);

        // הוספת משרות שפורסמו ע"י המשתמש
        model.addAttribute("myPositions", positionService.getPositionsByPublisher(principal.getName()));

        return "dashboard";
    }


}
