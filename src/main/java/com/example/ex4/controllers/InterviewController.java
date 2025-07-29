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

    /*@PostMapping("/schedule")
    public String scheduleInterview(@RequestParam Long applicationId,
                                    @RequestParam String interviewDate,
                                    @RequestParam String location,
                                    @RequestParam(required = false) String notes,
                                    @RequestParam(required = false) Boolean isVirtual,
                                    RedirectAttributes redirectAttributes) {
        try {
            Application application = applicationRepository.findById(applicationId).orElseThrow();
            java.time.LocalDateTime date = java.time.LocalDateTime.parse(interviewDate);
            interviewService.scheduleInterview(application, date, location, notes, isVirtual);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון נקבע בהצלחה!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בקביעת הראיון. ודא שכל השדות תקינים ונסה שוב.");
        }
        return "redirect:/dashboard";
    }*/

    @PostMapping("/{id}/confirm")
    public String confirmInterview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            interviewService.confirmInterview(id);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון אושר בהצלחה!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה באישור הראיון.");
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/reject")
    public String rejectInterview(@PathVariable Long id, @RequestParam(required = false) String reason, RedirectAttributes redirectAttributes) {
        try {
            interviewService.rejectInterview(id, reason);
            redirectAttributes.addFlashAttribute("successMessage", "הראיון נדחה בהצלחה!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בדחיית הראיון.");
        }
        return "redirect:/dashboard";
    }

    /*@PostMapping("/{id}/cancel")
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
        return "redirect:/dashboard";
    }*/

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
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/summary")
    public String updateInterviewSummary(@PathVariable Long id, @RequestParam String summary, RedirectAttributes redirectAttributes) {
        try {
            Interview interview = interviewService.getInterviewById(id);
            interview.setInterviewSummary(summary);
            interviewService.saveInterview(interview);
            redirectAttributes.addFlashAttribute("successMessage", "סיכום הראיון נשמר בהצלחה.");
            return "redirect:/application/" + interview.getApplication().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בשמירת סיכום הראיון.");
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
            } else if (decision.equals("CANCELED")) {
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

    /*@PostMapping("/{id}/edit")
    public String editInterview(@PathVariable Long id, 
                               @RequestParam String interviewDate,
                               @RequestParam String location,
                               @RequestParam(required = false) String notes,
                               @RequestParam(required = false) Boolean isVirtual,
                               RedirectAttributes redirectAttributes) {
        Long positionId = null;
        try {
            Interview interview = interviewService.getInterviewById(id);
            if (interview != null) {
                LocalDateTime newDate = LocalDateTime.parse(interviewDate);
                boolean dateChanged = !interview.getInterviewDate().equals(newDate);
                
                interview.setInterviewDate(newDate);
                interview.setLocation(location);
                interview.setNotes(notes);
                interview.setIsVirtual(isVirtual);
                
                if (dateChanged) {
                    interview.setStatus(InterviewStatus.SCHEDULED);
                    redirectAttributes.addFlashAttribute("successMessage", "הראיון עודכן בהצלחה. הסטטוס שונה ל'ממתין לאישור' עקב שינוי בתאריך/שעה.");
                } else {
                    redirectAttributes.addFlashAttribute("successMessage", "הראיון עודכן בהצלחה.");
                }
                
                interviewService.saveInterview(interview);
                if (interview.getApplication() != null && interview.getApplication().getPosition() != null) {
                    positionId = interview.getApplication().getPosition().getId();
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "אירעה שגיאה בעדכון הראיון.");
        }
        if (positionId != null) {
            return "redirect:/application/" + interviewService.getInterviewById(id).getApplication().getId();
        } else {
            return "redirect:/dashboard";
        }
    }*/
} 