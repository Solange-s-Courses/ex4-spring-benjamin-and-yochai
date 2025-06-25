package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.Position;
import com.example.ex4.services.PositionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        model.addAttribute("positionForm", new PositionForm());
        List<String> jobTitles = positionService.getAllDistinctJobTitles();
        model.addAttribute("jobTitles", jobTitles);
        return "add-position";
    }

    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model) {

        if (result.hasErrors()) {
            List<String> jobTitles = positionService.getAllDistinctJobTitles();
            model.addAttribute("jobTitles", jobTitles);
            return "add-position"; //maybe error page?
        }

        /*if (StringUtils.hasText(positionForm.getOtherJobTitle()) &&
                positionService.existsByJobTitle(positionForm.getOtherJobTitle())) {

            result.rejectValue("otherJobTitle", "error.Position", "המקצוע כבר קיים ברשימה");
            List<String> jobTitles = positionService.getAllDistinctJobTitles();
            model.addAttribute("jobTitles", jobTitles);
            return "add-position";
        }*/

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
        position.setRequirements(positionForm.getRequirements());
        positionService.save(position);
        return "redirect:/positions/add?success";
    }
}
