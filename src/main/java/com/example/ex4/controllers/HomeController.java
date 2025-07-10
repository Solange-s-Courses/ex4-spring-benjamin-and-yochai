package com.example.ex4.controllers;

import com.example.ex4.models.LocationRegion;
import org.springframework.ui.Model;
import com.example.ex4.models.AppUser;
import com.example.ex4.models.Position;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {  return "about"; }

    @GetMapping("/home")
    public String home1(Model model) {
        // משתמש מדומה
        AppUser mockUser = new AppUser();
        mockUser.setUsername("משתמש");

        // רשימת משרות ריקה או עם נתונים דמה
        List<Position> mockJobs = new ArrayList<>();

        // דוגמה ל־Job אחד (לא חובה אם אתה רק בודק עיצוב ריק)
        Position job = new Position();
        job.setId(1L);
        job.setJobTitle("מפתח Full Stack");
        job.setLocation(LocationRegion.מרכז);
        job.setAssignmentType("עורפי");
        job.setRequirements("Java Spring, Boot, SQL");
        job.setDescription("פיתוח מערכת ניהול חיילים בזמן אמת");
        mockJobs.add(job);

        // סינון מדומה
        Map<String, String> filters = new HashMap<>();
        filters.put("location", "");

        model.addAttribute("user", mockUser);
        model.addAttribute("jobs", mockJobs);
        model.addAttribute("filters", filters);

        return "main-page";
    }
} 