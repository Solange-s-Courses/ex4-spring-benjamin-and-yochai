package com.example.ex4.controllers;

import com.example.ex4.models.*;
import com.example.ex4.services.*;
import com.example.ex4.dto.LoginForm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {
    @Autowired
    private DashboardService dashboardService;

    /**
     * Displays the home page
     * 
     * @return The name of the home page template
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Displays the about page
     * 
     * @return The name of the about page template
     */
    @GetMapping("/about")
    public String about() {  return "about"; }

    /**
     * Displays the login page with error handling
     * 
     * @param model Spring MVC model
     * @param request HTTP request object
     * @return The name of the login page template
     */
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        LoginForm loginForm = new LoginForm();
        
        // Check if there's a username saved from previous failed login
        // Get from session attributes
        String savedUsername = (String) request.getSession().getAttribute("savedUsername");
        if (savedUsername != null) {
            loginForm.setUsername(savedUsername);
            // Clear the session attribute after using it
            request.getSession().removeAttribute("savedUsername");
        }
        
        // Check if there's an error message from session
        String loginError = (String) request.getSession().getAttribute("loginError");
        if (loginError != null) {
            model.addAttribute("loginError", loginError);
            // Clear the session attribute after using it
            request.getSession().removeAttribute("loginError");
        }
        
        model.addAttribute("loginForm", loginForm);
        return "login";
    }

    /**
     * Displays the user dashboard with applications and interviews
     * 
     * @param model Spring MVC model
     * @param principal Current authenticated user
     * @return The name of the dashboard template or redirect to login
     */
    @GetMapping("/dashboard")
    public String getDashboard(Model model, Principal principal) {
        return dashboardService.getDashboard(model, principal);

    }
    

}
