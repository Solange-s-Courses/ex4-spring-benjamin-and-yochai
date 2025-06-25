package com.example.ex4.controllers;

import com.example.ex4.dto.RegistrationForm;
import com.example.ex4.services.AppUserService;
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

    private final AppUserService appUserService;

    @Autowired
    public RegistrationController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String registerUser(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult result,
                               RedirectAttributes redirectAttributes){

        if (form.getMilitaryIdDoc() == null || form.getMilitaryIdDoc().isEmpty()) {
            result.rejectValue("militaryIdDoc", "error.militaryIdDoc", "חובה להעלות תעודת משרת מילואים!");
        }

        if (result.hasErrors()) {
            return "register"; //maybe error page?
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
            //return "redirect:/login";
            return "redirect:/register";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך ההרשמה");
            return "redirect:/register"; //error page? why not return "register"?
        }
    }
} 