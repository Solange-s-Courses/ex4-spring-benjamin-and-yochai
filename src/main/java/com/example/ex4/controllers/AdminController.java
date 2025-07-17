package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.services.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppUserService appUserService;

    @GetMapping
    public String getAdminDashboard(Model model) {
        List<AppUser> pendingUsers = appUserService.getPendingUsers();
        List<AppUser> allUsers = appUserService.getAllUsers();

        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("allUsers", allUsers);

        return "admin-dashboard";
    }

}