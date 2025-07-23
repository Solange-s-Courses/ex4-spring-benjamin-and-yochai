package com.example.ex4.controllers;

import com.example.ex4.models.Application;
import com.example.ex4.models.Interview;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/application")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private InterviewService interviewService;

    @GetMapping("/{id}")
    public String application(@PathVariable Integer id, Model model, Principal principal) {
        Application app = applicationService.getApplicationById(id);

        if (!principal.getName().equals(app.getPosition().getPublisher().getUsername())){
            return "redirect:error";
        }

        List<Interview> interviews = interviewService.getInterviewsByApplication(app);

        model.addAttribute("app", app);
        model.addAttribute("interviews", interviews);

        System.out.println("app:" + app);
        System.out.println("interviews: " + interviews);

        return "application-details";
    }
}
