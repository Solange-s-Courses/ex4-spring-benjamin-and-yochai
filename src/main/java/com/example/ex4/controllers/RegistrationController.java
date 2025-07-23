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

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        model.addAttribute("maxFileSize", maxFileSize);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult result,
                               RedirectAttributes redirectAttributes){

        if (form.getMilitaryIdDoc() == null || form.getMilitaryIdDoc().isEmpty()) {
            result.rejectValue("militaryIdDoc", "error.militaryIdDoc", "חובה להעלות תעודת משרת מילואים!");
        }
        else {
            String contentType = form.getMilitaryIdDoc().getContentType();
            String fileName = form.getMilitaryIdDoc().getOriginalFilename();
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf") || fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                result.rejectValue("militaryIdDoc", "error.militaryIdDoc", "יש להעלות קובץ PDF בלבד");
            }
        }

        if (result.hasErrors()) {
            return "register";
        }

        if (appUserService.existsByUsername(form.getUsername())) {
            result.rejectValue("username", "error.appUser", "שם המשתמש כבר קיים");
            return "register";
        }

        if (appUserService.existsByEmail(form.getEmail())) {
            result.rejectValue("email", "error.appUser", "האימייל כבר קיים");
            return "register";
        }

        try{
            appUserService.saveUser(form);
            redirectAttributes.addFlashAttribute("successMessage", "נרשמת בהצלחה!");
            return "redirect:/login";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך ההרשמה, אנא נסו שנית במועד מאוחר יותר.");
            return "redirect:/register";
        }
    }
} 