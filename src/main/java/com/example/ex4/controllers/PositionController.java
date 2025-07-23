package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.Application;
import com.example.ex4.models.ApplicationStatus;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.PositionService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.InterviewService;
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
    private InterviewService interviewService;
    @Autowired
    private PositionRepository positionRepository;

    @GetMapping("")
    public String positionsPage(Model model, Principal principal) {
        return positionService.getPositionsPage(model);
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable Long id, Model model, Principal principal) {
        String result = positionService.getPosition(id, model);

        Application userApplication = applicationService.getUserApplicationForPosition(id, principal.getName());
        if (userApplication != null) {
            model.addAttribute("userApplication", userApplication);
            boolean hasApplied = userApplication.getStatus() != ApplicationStatus.CANCELED;
            model.addAttribute("hasApplied", hasApplied);
        } else {
            model.addAttribute("hasApplied", false);
        }
        Position position = (Position) model.getAttribute("position");
        AppUser currentUser = appUserService.getUserByUsername(principal.getName());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = position.getPublisher().getUsername().equals(principal.getName());
        if (isAdmin || isOwner) {
            List<Application> applications = applicationService.getApplicationsByPositionId(id);
            model.addAttribute("applications", applications);
        }

        return result;
    }

    /*@PutMapping("/{id")
    public String editPosition(@PathVariable Long id, Model model, Principal principal) {

    }*/

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

    /*@PostMapping("/{id}/apply")
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
    }*/

    /*@PostMapping("/{id}/cancel")
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
    }*/

    /*@GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMMANDER')")
    public String getMyPositions(Model model, Principal principal) {
        List<Position> myPositions = positionService.getPositionsByPublisher(principal.getName());
        model.addAttribute("myPositions", myPositions);
        return "my-positions"; // תבנית חדשה
    }*/

    /*@GetMapping("/{id}/applicants")
    public String viewApplicants(@PathVariable Long id, Model model, Principal principal) {
        // בדיקה שהמשרה שייכת למשתמש הנוכחי או שהוא אדמין
        Position position = positionService.findById(id);
        if (position == null) {
            return "redirect:/dashboard";
        }
        
        AppUser currentUser = appUserService.getUserByUsername(principal.getName());
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = position.getPublisher().getUsername().equals(principal.getName());
        
        if (!isAdmin && !isOwner) {
            return "redirect:/dashboard";
        }
        
        List<Application> applications = applicationService.getApplicationsByPositionId(id);
        // הוספת ראיונות לכל מועמד
        java.util.Map<Long, java.util.List<com.example.ex4.models.Interview>> interviewsByApplication = new java.util.HashMap<>();
        for (Application app : applications) {
            interviewsByApplication.put(app.getId(), interviewService.getInterviewsByApplication(app));
        }
        model.addAttribute("applications", applications);
        model.addAttribute("position", position);
        model.addAttribute("interviewsByApplication", interviewsByApplication);
        return "applicants-list";
    }*/

    /*@PostMapping("/{id}/status")
    public String changePositionStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        boolean success = positionService.changePositionStatus(id, status, principal.getName());
        
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "סטטוס המשרה עודכן בהצלחה!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "שגיאה בעדכון סטטוס המשרה.");
        }
        return "redirect:/dashboard";
    }*/

}
