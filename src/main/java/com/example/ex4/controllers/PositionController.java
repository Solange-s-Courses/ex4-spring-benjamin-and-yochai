package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.Application;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.PositionService;
import com.example.ex4.services.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import com.example.ex4.models.Position;
import com.example.ex4.repositories.PositionRepository;
import com.example.ex4.models.AppUser;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private PositionRepository positionRepository;

    @GetMapping("")
    public String positionsPage(Model model, Principal principal) {
        return positionService.getPositionsPage(model);
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable Long id, Model model, Principal principal) {
        String result = positionService.getPosition(id, model);
        
        // Check if user has already applied
        if (principal != null) {
            // בדוק אם יש מועמדות כלשהי (פעילה או מבוטלת)
            Application userApplication = applicationService.getUserApplicationForPosition(id, principal.getName());
            if (userApplication != null) {
                model.addAttribute("userApplication", userApplication);
                // hasApplied יהיה true רק אם המועמדות פעילה (לא מבוטלת)
                boolean hasApplied = userApplication.getStatus() != com.example.ex4.models.ApplicationStatus.CANCELED;
                model.addAttribute("hasApplied", hasApplied);
            } else {
                model.addAttribute("hasApplied", false);
            }
        }
        
        return result;
    }

    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        return positionService.getAddPositionForm(model);
    }

    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processAddPositionForm(positionForm, model, result, principal.getName(), redirectAttributes);
    }

    @PostMapping("/{id}/apply")
    public String applyForPosition(@PathVariable Long id, 
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        boolean isResubmission = applicationService.isResubmission(id, principal.getName());
        boolean success = applicationService.submitApplication(id, principal.getName());
        
        if (success) {
            if (isResubmission) {
                redirectAttributes.addFlashAttribute("successMessage", "המועמדות הוגשה מחדש בהצלחה!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "המועמדות הוגשה בהצלחה!");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "שגיאה בהגשת המועמדות. ייתכן שכבר הגשת מועמדות פעילה למשרה זו.");
        }
        
        return "redirect:/positions/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelApplication(@PathVariable Long id, 
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        boolean success = applicationService.cancelApplication(id, principal.getName());
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "המועמדות בוטלה בהצלחה!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "שגיאה בביטול המועמדות.");
        }
        
        return "redirect:/positions/" + id;
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMANDER')")
    public String getMyPositions(Model model, Principal principal) {
        List<Position> myPositions = positionService.getPositionsByPublisher(principal.getName());
        model.addAttribute("myPositions", myPositions);
        return "my-positions"; // תבנית חדשה
    }

}
