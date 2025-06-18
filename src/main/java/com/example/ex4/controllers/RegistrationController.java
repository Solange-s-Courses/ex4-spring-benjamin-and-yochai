package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.repositories.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private AppUserRepository appUserRepository;

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new AppUser());
        return "register";
    }

    /*@PostMapping
    public String registerUser(@Valid @ModelAttribute("user") AppUser appUser,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        // Check if username already exists
        if (appUserRepository.existsByUsername(appUser.getUsername())) {
            result.rejectValue("username", "error.user", "שם המשתמש כבר קיים במערכת");
            return "register";
        }

        // Check if email already exists
        if (appUserRepository.existsByEmail(appUser.getEmail())) {
            result.rejectValue("email", "error.user", "כתובת האימייל כבר קיימת במערכת");
            return "register";
        }

        // Save the user
        appUserRepository.save(appUser);

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "ההרשמה הושלמה בהצלחה! אנא התחבר למערכת.");
        
        return "redirect:/login";
    }*/
} 