package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Position;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.PositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;
    @Autowired
    private AppUserService appUserService;

    @GetMapping("")
    public String positionsPage(Model model, Principal principal) {
        return positionService.getPositions(model);
    }

    @GetMapping("/{id}")
    public String getPosition(@PathVariable Long id, Model model) {
        return positionService.getPosition(id, model);
    }

    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        PositionForm form = new PositionForm();
        form.setRequirements(Arrays.asList(""));
        model.addAttribute("positionForm", form);
        List<String> jobTitles = positionService.getAllDistinctJobTitles();
        model.addAttribute("jobTitles", jobTitles);
        return "add-position";
    }

    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes, Principal principal) {

        if (result.hasErrors()) {
            List<String> jobTitles = positionService.getAllDistinctJobTitles();
            model.addAttribute("jobTitles", jobTitles);
            return "add-position";
        }

        Position position = new Position();
        // Handle jobTitle/otherJobTitle logic
        if (StringUtils.hasText(positionForm.getOtherJobTitle())) {
            position.setJobTitle(positionForm.getOtherJobTitle());
        } else {
            position.setJobTitle(positionForm.getJobTitle());
        }
        position.setLocation(positionForm.getLocation());
        position.setAssignmentType(positionForm.getAssignmentType());
        position.setDescription(positionForm.getDescription());

        String processedRequirements = positionService.processRequirements(positionForm.getRequirements());
        position.setRequirements(processedRequirements);

        AppUser publisher = appUserService.getUserByUsername(principal.getName());
        position.setPublisher(publisher);

        try{
            positionService.save(position);
            redirectAttributes.addFlashAttribute("successMessage", "המשרה הוספה בהצלחה!");
            return "redirect:/positions";
        } catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בתהליך השמירה, אנא נסו שנית במועד מאוחר יותר.");
            return "add-position";
        }
    }
}
