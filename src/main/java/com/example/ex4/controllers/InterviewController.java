package com.example.ex4.controllers;

import com.example.ex4.models.*;
import com.example.ex4.services.InterviewService;
import com.example.ex4.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.time.LocalDateTime;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/interviews")
public class InterviewController {
    @Autowired
    private InterviewService interviewService;
    @Autowired
    private ApplicationRepository applicationRepository;

    @PostMapping("/schedule")
    public String scheduleInterview(@RequestParam Long applicationId,
                                    @RequestParam String interviewDate,
                                    @RequestParam String location,
                                    @RequestParam(required = false) String notes,
                                    RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationRepository.findById(applicationId).orElseThrow();
            java.time.LocalDateTime date = java.time.LocalDateTime.parse(interviewDate);
            interviewService.scheduleInterview(application, date, location, notes);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון נקבע בהצלחה!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בקביעת הראיון. ודא שכל השדות תקינים ונסה שוב.");
        }
        // חזרה לרשימת המועמדים של המשרה
        Long positionId = null;
        try {
            Application application = applicationRepository.findById(applicationId).orElse(null);
            if (application != null && application.getPosition() != null) {
                positionId = application.getPosition().getId();
            }
        } catch (Exception ignore) {}
        if (positionId != null) {
            return "redirect:/positions/" + positionId + "/applicants";
        } else {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/confirm")
    public String confirmInterview(@PathVariable Long id) {
        interviewService.confirmInterview(id);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/reject")
    public String rejectInterview(@PathVariable Long id, @RequestParam String reason) {
        interviewService.rejectInterview(id, reason);
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/cancel")
    public String cancelInterview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long positionId = null;
        try {
            Interview interview = interviewService.getInterviewById(id);
            interviewService.cancelInterview(id);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון בוטל בהצלחה.");
            if (interview != null && interview.getApplication() != null && interview.getApplication().getPosition() != null) {
                positionId = interview.getApplication().getPosition().getId();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בביטול הראיון.");
        }
        if (positionId != null) {
            return "redirect:/positions/" + positionId + "/applicants";
        } else {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/complete")
    public String completeInterview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long positionId = null;
        try {
            Interview interview = interviewService.getInterviewById(id);
            interviewService.completeInterview(id);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון סומן כהושלם.");
            if (interview != null && interview.getApplication() != null && interview.getApplication().getPosition() != null) {
                positionId = interview.getApplication().getPosition().getId();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בסימון הראיון כהושלם.");
        }
        if (positionId != null) {
            return "redirect:/positions/" + positionId + "/applicants";
        } else {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/summary")
    public String updateInterviewSummary(@PathVariable Long id, @RequestParam String summary, RedirectAttributes redirectAttributes) {
        Long positionId = null;
        try {
            Interview interview = interviewService.getInterviewById(id);
            interview.setInterviewSummary(summary);
            interviewService.saveInterview(interview);
            redirectAttributes.addFlashAttribute("successMessage", "סיכום הראיון נשמר בהצלחה.");
            if (interview != null && interview.getApplication() != null && interview.getApplication().getPosition() != null) {
                positionId = interview.getApplication().getPosition().getId();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בשמירת סיכום הראיון.");
        }
        if (positionId != null) {
            return "redirect:/positions/" + positionId + "/applicants";
        } else {
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/decision")
    public String changeInterviewDecision(@PathVariable Long id, @RequestParam String decision, RedirectAttributes redirectAttributes) {
        Long positionId = null;
        try {
            Interview interview = interviewService.getInterviewById(id);
            if (decision.equals("COMPLETED")) {
                interviewService.completeInterview(id);
            } else if (decision.equals("REJECTED")) {
                interviewService.cancelInterview(id); // treat as rejected/canceled
            } else {
                // Set to SCHEDULED (pending)
                interview.setStatus(InterviewStatus.SCHEDULED);
                interviewService.saveInterview(interview);
            }
            redirectAttributes.addFlashAttribute("successMessage", "ההחלטה עודכנה בהצלחה.");
            if (interview != null && interview.getApplication() != null && interview.getApplication().getPosition() != null) {
                positionId = interview.getApplication().getPosition().getId();
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בעדכון ההחלטה.");
        }
        return "redirect:/dashboard";
    }
} 