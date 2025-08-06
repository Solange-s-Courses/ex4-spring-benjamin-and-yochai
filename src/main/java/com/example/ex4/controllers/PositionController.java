package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.services.PositionService;
import com.example.ex4.services.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @Autowired
    private ApplicationService applicationService;

    @GetMapping("")
    public String positionsPage(Model model, HttpSession session) {
        return positionService.getPositionsPage(model, session);
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        String result = positionService.getPositionPage(id, model);

        Application userApplication = applicationService.getUserApplicationForPosition(id, principal.getName());

        if (userApplication != null && userApplication.getStatus() != ApplicationStatus.CANCELED) {
            model.addAttribute("userApplication", userApplication);
        }

        List<Application> applications = applicationService.getApplicationsByPositionId(id);
        model.addAttribute("applications", applications);

        return result;
    }

    @GetMapping("/edit/{id}")
    public String editPositionPage(@PathVariable Long id, Model model, Principal principal) {
        return positionService.getEditPosition(id, model, principal);
    }

    @PutMapping("/{id}")
    public String editPosition(@PathVariable Long id, @Valid @ModelAttribute("positionForm") PositionForm positionForm,
                               BindingResult result,
                               RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processEditPositionForm(id ,positionForm, result, principal.getName(), redirectAttributes);
    }

    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        return positionService.getAddPositionForm(model);
    }

    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processAddPositionForm(positionForm, result, principal.getName(), redirectAttributes);
    }

}
