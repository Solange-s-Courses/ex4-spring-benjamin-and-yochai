package com.example.ex4.dto;

public class InterviewDecisionForm {
    private Long interviewId;
    private String decision; // "CONFIRM" or "REJECT"
    private String rejectionReason;

    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
} 