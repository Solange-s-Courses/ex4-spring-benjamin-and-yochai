package com.example.ex4.controllers;

import com.example.ex4.models.AppUser;
import com.example.ex4.models.RegistrationStatus;
import com.example.ex4.models.Role;
import com.example.ex4.services.AppUserService;
import com.example.ex4.services.ApplicationService;
import com.example.ex4.services.PositionService;
import com.example.ex4.dto.PositionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;


@Controller
@RequestMapping("/restapi")
public class RestApiController {
    @Autowired
    private AppUserService appUserService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private ApplicationService applicationService;

    public static class RecentSearch implements Serializable {
        public String search;
        public String location;
        public String serviceType;
        public RecentSearch(String search, String location, String serviceType) {
            this.search = search;
            this.location = location;
            this.serviceType = serviceType;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RecentSearch)) return false;
            RecentSearch other = (RecentSearch) o;
            return java.util.Objects.equals(search, other.search)
                && java.util.Objects.equals(location, other.location)
                && java.util.Objects.equals(serviceType, other.serviceType);
        }
        @Override
        public int hashCode() {
            return java.util.Objects.hash(search, location, serviceType);
        }
    }

    @GetMapping("/admin/document/{id}")
    public ResponseEntity<byte[]> getDocument(@PathVariable Long id) {
        Optional<AppUser> userOpt = appUserService.getUserById(id);
        if (userOpt.isPresent()) {
            byte[] data = userOpt.get().getMilitaryIdDoc();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"military_id.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF) // קובץ PDF בלבד
                    .body(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/admin/allUsers")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @PostMapping("/admin/changeUserStatus")
    public ResponseEntity<AppUser> changeUserStatus(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newStatus = body.get("status");

        return appUserService.changeUserStatus(userId, RegistrationStatus.valueOf(newStatus));
    }

    @PostMapping("/admin/changeUserRole")
    public ResponseEntity<AppUser> changeUserRole(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String newRole = body.get("role");

        return appUserService.changeUserRole(userId, Role.valueOf(newRole));
    }

    @GetMapping("/positions")
    @ResponseBody
    public ResponseEntity<List<PositionDto>> searchPositions(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "serviceType", required = false) String assignmentType,
            HttpSession session) {
        // שמירת חיפוש אחרון כולל פילטרים
        if ((search != null && !search.isBlank()) || (location != null && !location.isBlank()) || (assignmentType != null && !assignmentType.isBlank())) {
            java.util.List<RecentSearch> recentSearches = (java.util.List<RecentSearch>) session.getAttribute("recent_searches");
            if (recentSearches == null) recentSearches = new java.util.LinkedList<>();
            RecentSearch newSearch = new RecentSearch(search, location, assignmentType);
            recentSearches.remove(newSearch);
            recentSearches.add(0, newSearch);
            if (recentSearches.size() > 5) recentSearches = recentSearches.subList(0, 5);
            session.setAttribute("recent_searches", recentSearches);
        }
        List<PositionDto> positions = positionService.searchPositions(
            search != null ? search : "",
            location,
            assignmentType
        );
        return ResponseEntity.ok(positions);
    }

    @GetMapping("/positions/recent-searches")
    @ResponseBody
    public java.util.List<RecentSearch> getRecentSearches(HttpSession session) {
        java.util.List<RecentSearch> recentSearches = (java.util.List<RecentSearch>) session.getAttribute("recent_searches");
        return recentSearches != null ? recentSearches : new java.util.LinkedList<>();
    }

    @GetMapping("/positions/active")
    public ResponseEntity<Map<String, Object>> getPositionsData() {
        return positionService.reloadPositions();
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Map<String, Object>> applyForPosition(@PathVariable Long id,
                                   Principal principal) {
        //return positionService.applyForPosition(id, principal);
        Map<String, Object> response = new HashMap<>();

        try {
            applicationService.submitApplication(id, principal.getName());
            response.put("message", "המועמדות הוגשה בהצלחה!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "אירעה שגיאה בהגשת המועמדות.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelApplication(@PathVariable Long id,
                                Principal principal) {
        return applicationService.cancelApplication(id, principal.getName());
    }

    @PostMapping("/positions/{id}/status")
    public ResponseEntity<Map<String, Object>> changePositionStatus(@PathVariable Long id,
                                                                   @RequestBody Map<String, String> body,
                                                                   Principal principal) {
        String status = body.get("status");
        return positionService.changePositionStatus(id, status, principal.getName());
    }

    
}

