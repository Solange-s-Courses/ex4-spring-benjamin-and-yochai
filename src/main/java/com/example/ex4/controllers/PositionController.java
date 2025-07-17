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
        return positionService.getAddPositionForm(model);
    }

    @PostMapping("/add")
    public String addPosition(@Valid @ModelAttribute("positionForm") PositionForm positionForm,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes, Principal principal) {
        return positionService.processAddPositionForm(positionForm, model, result, principal.getName(), redirectAttributes);
    }
}
