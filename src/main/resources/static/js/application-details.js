import {showToast} from "./toastUtils";

const applicationDetailsDom = function() {
    
    document.addEventListener('DOMContentLoaded', function () {
        
        async function completeInterview(interviewId) {
            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                const response = await fetch(`/restapi/interviews/${interviewId}/complete`, {
                    method: 'POST',
                    headers: headers,
                    credentials: "include"
                });

                const data = await response.json();

                if (response.ok) {
                    showToast(data.message);
                    //setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בהשלמת הראיון", "danger");
            }
        }

        async function updateInterviewSummary(interviewId, summary) {
            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                const response = await fetch(`/restapi/interviews/${interviewId}/summary`, {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify({ summary: summary }),
                    credentials: "include"
                });

                const data = await response.json();

                if (response.ok) {
                    showToast(data.message);
                    //setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בעדכון סיכום הראיון", "danger");
            }
        }

        async function setCompleteInterviewListener(form, event) {
            event.preventDefault();
            const interviewId = form.dataset.interviewId;
            await completeInterview(interviewId);
        }

        async function setUpdateSummaryListener(form, event) {
            event.preventDefault();
            const interviewId = form.dataset.interviewId;
            const summary = form.querySelector('textarea[name="summary"]').value;
            await updateInterviewSummary(interviewId, summary);
        }

        // הוספת event listeners לפורמים
        const completeForms = document.querySelectorAll('.complete-interview-form');
        completeForms.forEach(form => {
            form.addEventListener('submit', async (event) => {
                await setCompleteInterviewListener(form, event);
            });
        });

        const updateSummaryForms = document.querySelectorAll('.update-summary-form');
        updateSummaryForms.forEach(form => {
            form.addEventListener('submit', async (event) => {
                await setUpdateSummaryListener(form, event);
            });
        });

    });
};

applicationDetailsDom(); 