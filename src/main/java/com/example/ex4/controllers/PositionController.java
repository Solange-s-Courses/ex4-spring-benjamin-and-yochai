package com.example.ex4.controllers;

import com.example.ex4.dto.PositionForm;
import com.example.ex4.models.Position;
import com.example.ex4.services.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.util.StringUtils;

@Controller
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping("/add")
    public String showAddPositionForm(Model model) {
        model.addAttribute("positionForm", new PositionForm());
        return "add-position";
    }

    @PostMapping("/add")
    public String addPosition(@ModelAttribute PositionForm positionForm) {
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
