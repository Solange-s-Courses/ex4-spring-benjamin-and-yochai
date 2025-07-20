package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
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
        // שליפת המשתמש לפי השם מתוך הפרינסיפל
        AppUser user = new AppUser();
        user.setFirstName(principal.getName());
        user.setRegistrationStatus(RegistrationStatus.APPROVED);

        // נתונים כלליים לתצוגה - כעת נשלוף אמיתי
        List<?> submittedApplications = new ArrayList<>(); // למועמדויות שהגשתי - לא משומש כרגע
        List<?> upcomingInterviews = new ArrayList<>(); // לראיונות עתידיים - לא משומש כרגע

        // סטטיסטיקות - לא משומש כרגע
        long totalApplications = 0;
        long pendingApplications = 0;
        long upcomingInterviewCount = 0;

        // שליחה ל־Thymeleaf
        model.addAttribute("user", user);
        model.addAttribute("applications", submittedApplications);
        model.addAttribute("interviews", upcomingInterviews);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("pendingApplications", pendingApplications);
        model.addAttribute("upcomingInterviewCount", upcomingInterviewCount);

        return "dashboard";
    }


}
