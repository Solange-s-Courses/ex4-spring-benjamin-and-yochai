import {showToast} from "./toastUtils.js";
import {formatDate, formatDateTime, formatTime, getApplicationStatusInfo, getInterviewStatusInfo} from "./textUtils.js";

const applicationDetailsDom = function() {
    
    document.addEventListener('DOMContentLoaded', function () {
        const applicationStatusBadge = document.getElementById("applicationStatusBadge");
        const actionsCard = document.getElementById("actionsCard");
        const interviewsTable = document.getElementById("interviews-table")
        const applicationId = document.getElementById("applicationId").dataset.id;
        
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
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בהשלמת הראיון", "danger");
            }finally {
                await reload();
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
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בעדכון סיכום הראיון", "danger");
            }finally {
                await reload();
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

        async function reload(){
            try{
                const response = await fetch(`/restapi/applications/${applicationId}/poll`, {
                    method: "GET"
                })
                if (!response.ok) throw Error("Failed to poll");

                const data = await response.json();

                const statusInfo = getApplicationStatusInfo(data.application.status);
                applicationStatusBadge.className =  `badge ${statusInfo.cssClass}`;
                applicationStatusBadge.textContent = statusInfo.text;

                if (data.application.status !== "PENDING"){
                    actionsCard.classList.add('d-none');
                }else{
                    actionsCard.classList.remove('d-none');
                }

                const tbody = interviewsTable.querySelector("tbody");

                for (const interview of data.interviews) {
                    let existingRow = tbody.querySelector(`tr[data-interview-id="${interview.id}"]`);
                    const statusInfo = getInterviewStatusInfo(interview.status);

                    let locationHtml = '';
                    if(interview.isVirtual === true) {
                        locationHtml = `
                            <div class="text-center">
                                <a href=${interview.jitsiLink} target="_blank"
                                    class="btn btn-sm btn-primary">
                                    <i class="bi bi-camera-video"></i> הצטרף לפגישה
                                </a>
                            </div>`;
                    } else {
                        locationHtml = `
                            <span>${interview.location}</span>
                        `;
                    }

                    if (existingRow) {
                        const cols = existingRow.querySelectorAll('td');
                        cols[0].textContent = formatDate(interview.interviewDate);
                        cols[1].textContent = formatTime(interview.interviewDate);
                        cols[2].innerHTML = locationHtml;

                        const badge = cols[3].querySelector("span");
                        badge.className = `badge ${statusInfo.cssClass}`;
                        badge.textContent = statusInfo.text;

                        cols[4].textContent = interview.notes;

                    }
                    else {
                        existingRow = document.createElement("tr");
                        existingRow.setAttribute("interview-id", interview.id);

                        existingRow.innerHTML = `
                            <td>${formatDate(interview.interviewDate)}</td>
                            <td>${formatTime(interview.interviewDate)}</td>
                            <td>
                                ${locationHtml}
                            </td>
                            <td>
                                <span class="badge ${statusInfo.cssClass}">${statusInfo.text}</span>
                            </td>
                            <td>${interview.notes}</td>
                            <td>
                                <form class="mt-2 update-summary-form" data-interview-id=${interview.id}>
                                    <div class="input-group input-group-sm">
                                        <textarea name="summary" class="form-control form-control-sm" rows="2" 
                                            placeholder='כתוב סיכום ראיון...'>${interview.interviewSummary}</textarea>
                                        <button type="submit" class="btn btn-outline-primary btn-sm">שמור</button>
                                    </div>
                                </form>
                            </td>
                            <td>
                                <div class="d-flex flex-column ms-1 d-inline-flex w-100">
                                    <button type="button"
                                        class="btn btn-outline-secondary btn-sm mb-1 edit-interview-btn w-100"
                                        data-id="${interview.id}" 
                                        data-date="${interview.interviewDate}" 
                                        data-location="${interview.location}" 
                                        data-notes="${interview.notes != null ? interview.notes : ''}"
                                        data-virtual="${interview.isVirtual != null ? interview.isVirtual : false}">
                                        <i class="bi bi-pencil"></i> ערוך
                                    </button>
                
                                    <form action="@{'/interviews/' + ${interview.id} + '/cancel'}"
                                        method="post" 
                                        class="d-inline cancel-interview-form">
                                        <button type="submit" class="btn btn-outline-danger btn-sm mb-1 w-100">
                                            <i class="bi bi-x-circle"></i> בטל
                                        </button>
                                    </form>
                
                                    <form class="d-inline complete-interview-form"
                                        data-interview-id=${interview.id}>
                                        <button type="submit" class="btn btn-outline-success btn-sm w-100">
                                            <i class="bi bi-check-circle"></i> הושלם
                                        </button>
                                    </form>
                                </div>
                            </td>`;

                        tbody.appendChild(existingRow);

                    }

                    const editButton = existingRow.querySelector(".edit-interview-btn");
                    const cancelForm = existingRow.querySelector(".cancel-interview-form");
                    const completeForm = existingRow.querySelector(".complete-interview-form");

                    switch (interview.status) {
                        case "SCHEDULED":
                            editButton.classList.remove("d-none");
                            cancelForm.classList.remove("d-none");
                            completeForm.classList.add("d-none");
                            break;
                        case "CONFIRMED":
                            editButton.classList.remove("d-none");
                            cancelForm.classList.remove("d-none");
                            completeForm.classList.remove("d-none");
                            break;

                        default:
                            editButton.classList.add("d-none");
                            cancelForm.classList.add("d-none");
                            completeForm.classList.add("d-none");
                            break;
                    }

                }
            }
            catch (e){
                console.error("error polling: " + e);
            }
        }

        const pollingInterval = setInterval(reload, 5000);

        window.addEventListener("beforeunload", ()=>{
            clearInterval(pollingInterval);
        })

    });
};

applicationDetailsDom(); 