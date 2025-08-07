import {showToast} from "./toastUtils.js";
import {formatDate, formatDateTime, formatTime, getApplicationStatusInfo, getInterviewStatusInfo} from "./textUtils.js";
import {genericSortingFunc, sortRowsByDate} from "./sortingFuncs.js";

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
                        existingRow.setAttribute("data-interview-id", interview.id);

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
                                            placeholder='כתוב סיכום ראיון...'>${interview.interviewSummary || ""}</textarea>
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
                                        class="d-inline cancel-interview-form"
                                        data-interview-id=${interview.id}>
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

                        //add event listener

                        const summaryForm = existingRow.querySelector(".update-summary-form");
                        summaryForm.addEventListener("submit", async (e)=>{
                            await setUpdateSummaryListener(summaryForm, e);
                        })

                        const btn = existingRow.querySelector(".edit-interview-btn");
                        btn.addEventListener("click",()=>{
                            openEditModal(btn);
                        });

                        const cancelForm = existingRow.querySelector(".cancel-interview-form");
                        cancelForm.addEventListener("submit", async (e)=>{
                            e.preventDefault();
                            await cancelInterview(interview.id);
                        });

                        const completeForm = existingRow.querySelector(".complete-interview-form");
                        completeForm.addEventListener("submit",async (e)=>{
                            await setCompleteInterviewListener(completeForm, e)
                        });

                        const sortType = interviewsTable.dataset.sortType;
                        if (sortType){
                            genericSortingFunc(interviewsTable, 0, sortRowsByDate, false);
                        }
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

        const toggleLocationField = (isVirtual, locationLabelId, locationInputId) => {
            const locationLabel = document.getElementById(locationLabelId);
            const locationInput = document.getElementById(locationInputId);

            if (isVirtual) {
                locationLabel.textContent = 'קישור לפגישה';
                locationInput.placeholder = 'קישור לשיחת וידאו ייווצר אוטומטית';
                locationInput.value = '';
                locationInput.readOnly = true;
                locationInput.classList.add('bg-light');
            } else {
                locationLabel.textContent = 'מיקום';
                locationInput.placeholder = 'מיקום פיזי';
                locationInput.readOnly = false;
                locationInput.classList.remove('bg-light');
            }
        };

        const toggleMeetingType = () => {
            const isVirtual = document.getElementById('meetingTypeSwitch').checked;
            toggleLocationField(isVirtual, 'locationLabel', 'locationInput');
            clearFieldError('locationInput');

        };

        const toggleEditMeetingType = () => {
            const isVirtual = document.getElementById('editMeetingTypeSwitch').checked;
            toggleLocationField(isVirtual, 'editLocationLabel', 'editLocationInput');
            clearFieldError('editLocationInput');
        };


        const editInterview = (interviewId, date, location, notes, isVirtual) => {
            const form = document.getElementById('editInterviewForm');
            form.setAttribute('data-interview-id', interviewId);

            document.getElementById('editInterviewDate').value = date;
            document.getElementById('editLocationInput').value = location;
            document.getElementById('editInterviewNotes').value = notes;
            document.getElementById('editMeetingTypeSwitch').checked = isVirtual === 'true';

            toggleLocationField(isVirtual === 'true', 'editLocationLabel', 'editLocationInput');

            const modal = new bootstrap.Modal(document.getElementById('editInterviewModal'));
            modal.show();
        };

        const showFieldError = (form, fieldName, message) => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            const errorDiv = form.querySelector(`#${field.id}-error`);

            if (field && errorDiv) {
                field.classList.add('is-invalid');
                errorDiv.textContent = message;
            }
        };

        const clearFieldError = (fieldId) => {
            const field = document.getElementById(fieldId);
            const errorDiv = document.getElementById(`${fieldId}-error`);

            if (field) {
                field.classList.remove('is-invalid');
            }
            if (errorDiv) {
                errorDiv.textContent = '';
            }
        };

        const clearAllErrors = (form) => {
            if (!form) return;

            const fields = form.querySelectorAll('.is-invalid');
            fields.forEach(field => {
                field.classList.remove('is-invalid');
            });

            const errorDivs = form.querySelectorAll('.invalid-feedback');
            errorDivs.forEach(div => {
                div.textContent = '';
            });
        };

        const validateInterviewForm = (data, formId) => {
            let isValid = true;
            const form = document.getElementById(formId);

            clearAllErrors(form);

            //const dateFieldId = formId === 'editInterviewForm' ? 'editInterviewDate' : 'interviewDate';
            if (!data.get("interviewDate")) {
                showFieldError(form, "interviewDate", 'חובה לבחור תאריך ושעה לראיון');
                isValid = false;
            } else if (new Date(data.get("interviewDate")) < new Date()) {
                showFieldError(form, "interviewDate", 'לא ניתן לקבוע ראיון בזמן שחלף');
                isValid = false;
            }

            //const locationFieldId = formId === 'editInterviewForm' ? 'editLocationInput' : 'locationInput';
            if (data.get("isVirtual") !== "on"  && (!data.get("location") || data.get("location").trim() === '')) {
                showFieldError(form, "location", 'חובה להזין מיקום לפגישה פיזית או לבחור בפגישה וירטואלית');
                isValid = false;
            }

            return isValid;
        };

        const now = new Date();
        const nowString = now.toISOString().slice(0, 16);

        const dateInputs = document.querySelectorAll('input[type="datetime-local"]');
        dateInputs.forEach(input => {
            input.min = nowString;
        });

        const meetingTypeSwitch = document.getElementById('meetingTypeSwitch');
        if (meetingTypeSwitch) {
            meetingTypeSwitch.addEventListener('change', toggleMeetingType);
        }

        const editMeetingTypeSwitch = document.getElementById('editMeetingTypeSwitch');
        if (editMeetingTypeSwitch) {
            editMeetingTypeSwitch.addEventListener('change', toggleEditMeetingType);
        }

        const scheduleForm = document.querySelector('form[action="/interviews/schedule"]');
        if (scheduleForm) {
            scheduleForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const formData = new FormData(this);

                const data = {
                    applicationId: formData.get('applicationId'),
                    interviewDate: formData.get('interviewDate'),
                    location: formData.get('location'),
                    notes: formData.get('notes'),
                    isVirtual: formData.get('isVirtual') === 'on'
                };

                if (!validateInterviewForm(formData, 'scheduleInterviewForm')) {
                    return;
                }

                try {
                    const response = await fetch('/restapi/interviews/schedule', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                        },
                        body: JSON.stringify(data)
                        //body: formData
                    });

                    const result = await response.json();

                    if (!response.ok) {
                        throw new Error(result.message || 'אירעה שגיאה בקביעת הראיון');
                    }

                    if (result.success) {
                        showToast(result.message);
                        this.reset();
                    } else {
                        showToast(result.message, "danger");
                    }

                } catch (error) {
                    showToast(error.message || 'אירעה שגיאה בקביעת הראיון', "danger");

                } finally {
                    await reload();
                }
                //
                // fetch('/restapi/interviews/schedule', {
                //     method: 'POST',
                //     headers: {
                //         'Content-Type': 'application/json',
                //         'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                //     },
                //     body: JSON.stringify(data)
                // })
                //     .then(response => {
                //         if (!response.ok) {
                //             return response.json().then(errorData => {
                //                 throw new Error(errorData.message || 'אירעה שגיאה בקביעת הראיון');
                //             });
                //         }
                //         return response.json();
                //     })
                //     .then(result => {
                //         if (result.success) {
                //             showToast(result.message);
                //             //setTimeout(() => window.location.reload(), 1500);
                //         } else {
                //             showToast(result.message, "danger");
                //         }
                //     })
                //     .catch(error => {
                //         showToast(error.message || 'אירעה שגיאה בקביעת הראיון', "danger");
                //     })
                //     .finally(){
                //     refreshData();
                // };
            });
        }

        const editForm = document.getElementById('editInterviewForm');
        if (editForm) {
            editForm.addEventListener('submit', async function(e) {
                e.preventDefault();

                const formData = new FormData(this);
                const interviewId = this.getAttribute('data-interview-id');

                const data = {
                    interviewDate: formData.get('interviewDate'),
                    location: formData.get('location'),
                    notes: formData.get('notes'),
                    isVirtual: formData.get('isVirtual') === 'on'
                };

                if (!validateInterviewForm(formData, 'editInterviewForm')) {
                    return;
                }

                try {
                    const response = await fetch(`/restapi/interviews/${interviewId}/edit`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                        },
                        body: JSON.stringify(data)
                        //body: formData
                    });

                    const result = await response.json();

                    if (!response.ok) {
                        throw new Error(result.message || 'אירעה שגיאה בעדכון הראיון');
                    }

                    if (result.success) {
                        showToast(result.message);
                        const modal = bootstrap.Modal.getInstance(document.getElementById('editInterviewModal'));
                        modal.hide();
                    } else {
                        showToast(result.message, "danger");
                    }

                } catch (error) {
                    showToast(error.message || 'אירעה שגיאה בעדכון הראיון', "danger");
                }finally {
                    await reload();
                }
            });
        }

        function openEditModal(btn){
            const id = btn.getAttribute('data-id');
            const date = btn.getAttribute('data-date');
            const location = btn.getAttribute('data-location');
            const notes = btn.getAttribute('data-notes');
            const isVirtual = btn.getAttribute('data-virtual');
            editInterview(id, date, location, notes, isVirtual);
        }

        const editInterviewBtns = document.querySelectorAll(".edit-interview-btn");
        editInterviewBtns.forEach(btn =>{
            btn.addEventListener("click", ()=>{
                openEditModal(btn);
            })
        })

        // document.body.addEventListener('click', function (e) {
        //     const btn = e.target.closest('.edit-interview-btn');
        //     if (btn) {
        //         const id = btn.getAttribute('data-id');
        //         const date = btn.getAttribute('data-date');
        //         const location = btn.getAttribute('data-location');
        //         const notes = btn.getAttribute('data-notes');
        //         const isVirtual = btn.getAttribute('data-virtual');
        //         editInterview(id, date, location, notes, isVirtual);
        //     }
        // });

        async function applicationDecision(applicationId, action){
            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const response = await fetch(`/restapi/applications/${applicationId}/${action}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                });

                const data = await response.json();

                if (response.ok) {
                    showToast(data.message);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast(error.message || "אירעה שגיאה בשמירה", "danger");
            }finally {
                await reload();
            }
        }

        const approveForm = document.querySelector('.approve-application-form');
        if (approveForm) {
            approveForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const applicationId = approveForm.dataset.applicationId;
                await applicationDecision(applicationId, "approve");

                // try {
                //     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                //     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                //
                //     const response = await fetch(`/restapi/applications/${applicationId}/approve`, {
                //         method: 'POST',
                //         headers: {
                //             'Content-Type': 'application/json',
                //             [csrfHeader]: csrfToken
                //         }
                //     });
                //
                //     const data = await response.json();
                //
                //     if (response.ok) {
                //         showToast(data.message);
                //     } else {
                //         showToast(data.message, "danger");
                //     }
                // } catch (error) {
                //     showToast(error.message || "אירעה שגיאה באישור המועמדות", "danger");
                // }finally {
                //     await reload();
                // }
            });
        }

        const rejectForm = document.querySelector('.reject-application-form');
        if (rejectForm) {
            rejectForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const applicationId = rejectForm.dataset.applicationId;

                await applicationDecision(applicationId, "reject");
                // try {
                //     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                //     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                //
                //     const response = await fetch(`/restapi/applications/${applicationId}/reject`, {
                //         method: 'POST',
                //         headers: {
                //             'Content-Type': 'application/json',
                //             [csrfHeader]: csrfToken
                //         }
                //     });
                //
                //     const data = await response.json();
                //
                //     if (response.ok) {
                //         showToast(data.message);
                //     } else {
                //         showToast(data.message, "danger");
                //     }
                // } catch (error) {
                //     showToast("אירעה שגיאה בדחיית המועמדות", "danger");
                // }finally {
                //     await reload();
                // }
            });
        }

        async function cancelInterview(id){
            try {
                const response = await fetch(`/restapi/interviews/${id}/cancel`, {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                    }
                });

                const result = await response.json();

                if (!response.ok) {
                    throw new Error(result.message || 'אירעה שגיאה בביטול הראיון');
                }

                showToast(result.message);
                await reload();

            } catch (error) {
                showToast(error.message || 'אירעה שגיאה בביטול הראיון', "danger");
            }
        }

        const cancelForms = document.querySelectorAll('.cancel-interview-form');
        cancelForms.forEach(form => {
            form.addEventListener('submit', async function(e) {
                e.preventDefault();
                const interviewId = this.getAttribute('data-interview-id');

                await cancelInterview(interviewId);
            });
        });

    });
};

applicationDetailsDom(); 