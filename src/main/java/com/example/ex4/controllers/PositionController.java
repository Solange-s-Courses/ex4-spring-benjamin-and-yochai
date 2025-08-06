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

    /**
     * Displays the positions page
     * 
     * @param model Spring MVC model
     * @param session HTTP session
     * @return The name of the positions page template
     */
    @GetMapping("")
    public String positionsPage(Model model, HttpSession session) {
        return positionService.getPositionsPage(model, session);
    }

    /**
     * Displays a specific position page
     * 
     * @param id Position ID
     * @param model Spring MVC model
     * @param principal Current authenticated user
     * @param request HTTP request object
     * @return The name of the position page template
     */
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

    /**
     * Displays the edit position form
     * 
     * @param id Position ID
     * @param model Spring MVC model
     * @param principal Current authenticated user
     * @return The name of the edit position template
     */
    @GetMapping("/edit/{id}")
    public String editPositionPage(@PathVariable Long id, Model model, Principal principal) {
        return positionService.getEditPosition(id, model, principal);
    }

    /**
     * Processes the edit position form submission
     * 
     * @param id Position ID
     * @param positionForm Form data
     * @param result Binding result for validation
     * @param redirectAttributes Redirect attributes for flash messages
     * @param principal Current authenticated user
     * @return Redirect URL or template name
     */
    @PutMapping("/{id}")
    public String editPosition(@PathVariable Long id, @Valid @ModelAttribute("positionForm") PositionForm positionForm,
                               BindingResult result,
                               RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processEditPositionForm(id ,positionForm, result, principal.getName(), redirectAttributes);
    }

    /**
     * Displays the add position form
     * 
     * @param model Spring MVC model
     * @return The name of the add position template
     */
    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        return positionService.getAddPositionForm(model);
    }

    /**
     * Processes the add position form submission
     * 
     * @param positionForm Form data
     * @param result Binding result for validation
     * @param model Spring MVC model
     * @param redirectAttributes Redirect attributes for flash messages
     * @param principal Current authenticated user
     * @return Redirect URL or template name
     */
    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processAddPositionForm(positionForm, result, principal.getName(), redirectAttributes);
    }

}
