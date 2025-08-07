package com.example.ex4.controllers;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final AppUserService appUserService;

    @Value("${spring.servlet.multipart.max-file-size:1MB}")
    private String maxFileSize;

    @Autowired
    public RegistrationController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Displays the registration form
     * 
     * @param model Spring MVC model
     * @return The name of the registration template
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        model.addAttribute("maxFileSize", maxFileSize);
        return "register";
    }

    /**
     * Processes user registration
     * 
     * @param form Registration form data
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes for flash messages
     * @return Redirect URL or template name
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult result,
                               RedirectAttributes redirectAttributes){
        return appUserService.registerUser(form, result, redirectAttributes);
    }
} 