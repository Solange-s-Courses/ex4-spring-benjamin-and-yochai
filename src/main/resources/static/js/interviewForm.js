/*import {showToast} from "./toastUtils.js";

function toggleLocationField(isVirtual, locationLabelId, locationInputId) {
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
}

function toggleMeetingType() {
    const isVirtual = document.getElementById('meetingTypeSwitch').checked;
    toggleLocationField(isVirtual, 'locationLabel', 'locationInput');
    clearFieldError('locationInput');

}

function toggleEditMeetingType() {
    const isVirtual = document.getElementById('editMeetingTypeSwitch').checked;
    toggleLocationField(isVirtual, 'editLocationLabel', 'editLocationInput');
    clearFieldError('editLocationInput');

}

window.toggleMeetingType = toggleMeetingType;

function openJitsiLink(link) {
    window.open(link, '_blank', 'width=1200,height=800');
} 

function editInterview(interviewId, date, location, notes, isVirtual) {
    const form = document.getElementById('editInterviewForm');
    form.setAttribute('data-interview-id', interviewId);

    document.getElementById('editInterviewDate').value = date;
    document.getElementById('editLocationInput').value = location;
    document.getElementById('editInterviewNotes').value = notes;
    document.getElementById('editMeetingTypeSwitch').checked = isVirtual === 'true';
    
    toggleLocationField(isVirtual === 'true', 'editLocationLabel', 'editLocationInput');

    const modal = new bootstrap.Modal(document.getElementById('editInterviewModal'));
    modal.show();
}

function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(`${fieldId}-error`);
    
    if (field && errorDiv) {
        field.classList.add('is-invalid');
        errorDiv.textContent = message;
    }
}

function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    const errorDiv = document.getElementById(`${fieldId}-error`);
    
    if (field) {
        field.classList.remove('is-invalid');
    }
    if (errorDiv) {
        errorDiv.textContent = '';
    }
}

function clearAllErrors(formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    
    const fields = form.querySelectorAll('.is-invalid');
    fields.forEach(field => {
        field.classList.remove('is-invalid');
    });
    
    const errorDivs = form.querySelectorAll('.invalid-feedback');
    errorDivs.forEach(div => {
        div.textContent = '';
    });
}

function validateInterviewForm(data, formId) {
    let isValid = true;
    
    clearAllErrors(formId);
    
    const dateFieldId = formId === 'editInterviewForm' ? 'editInterviewDate' : 'interviewDate';
    if (!data.interviewDate) {
        showFieldError(dateFieldId, 'חובה לבחור תאריך ושעה לראיון');
        isValid = false;
    } else if (new Date(data.interviewDate) < new Date()) {
        showFieldError(dateFieldId, 'לא ניתן לקבוע ראיון בזמן שחלף');
        isValid = false;
    }
    
    const locationFieldId = formId === 'editInterviewForm' ? 'editLocationInput' : 'locationInput';
    if (!data.isVirtual && (!data.location || data.location.trim() === '')) {
        showFieldError(locationFieldId, 'חובה להזין מיקום לפגישה פיזית או לבחור בפגישה וירטואלית');
        isValid = false;
    }
    
    return isValid;
}

document.addEventListener('DOMContentLoaded', function () {
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
        scheduleForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const data = {
                applicationId: formData.get('applicationId'),
                interviewDate: formData.get('interviewDate'),
                location: formData.get('location'),
                notes: formData.get('notes'),
                isVirtual: formData.get('isVirtual') === 'on'
            };
            
            if (!validateInterviewForm(data, 'scheduleInterviewForm')) {
                return;
            }
            
            fetch('/restapi/interviews/schedule', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorData => {
                        throw new Error(errorData.message || 'אירעה שגיאה בקביעת הראיון');
                    });
                }
                return response.json();
            })
            .then(result => {
                if (result.success) {
                    showToast(result.message);
                    //setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(result.message, "danger");
                }
            })
            .catch(error => {
                showToast(error.message || 'אירעה שגיאה בקביעת הראיון', "danger");
            });
        });
    }
    

    const editForm = document.getElementById('editInterviewForm');
    if (editForm) {
        editForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(this);
            const interviewId = this.getAttribute('data-interview-id');
            const data = {
                interviewDate: formData.get('interviewDate'),
                location: formData.get('location'),
                notes: formData.get('notes'),
                isVirtual: formData.get('isVirtual') === 'on'
            };
            
            if (!validateInterviewForm(data, 'editInterviewForm')) {
                return;
            }
            
            fetch(`/restapi/interviews/${interviewId}/edit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(errorData => {
                        throw new Error(errorData.message || 'אירעה שגיאה בעדכון הראיון');
                    });
                }
                return response.json();
            })
            .then(result => {
                if (result.success) {
                    showToast(result.message);
                    const modal = bootstrap.Modal.getInstance(document.getElementById('editInterviewModal'));
                    modal.hide();
                    //setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(result.message, "danger");
                }
            })
            .catch(error => {
                showToast(error.message || 'אירעה שגיאה בעדכון הראיון', "danger");
            });
        });
    }
    
    document.body.addEventListener('click', function (e) {
        const btn = e.target.closest('.edit-interview-btn');
        if (btn) {
            const id = btn.getAttribute('data-id');
            const date = btn.getAttribute('data-date');
            const location = btn.getAttribute('data-location');
            const notes = btn.getAttribute('data-notes');
            const isVirtual = btn.getAttribute('data-virtual');
            editInterview(id, date, location, notes, isVirtual);
        }
    });

    // document.body.addEventListener('submit', function (e) {
    //     const form = e.target.closest('.cancel-interview-form');
    //     if (form && !confirm('האם אתה בטוח שברצונך לבטל את הראיון?')) {
    //         e.preventDefault();
    //     }
    // });

    const approveForm = document.querySelector('.approve-application-form');
    if (approveForm) {
        approveForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const applicationId = approveForm.dataset.applicationId;
            
            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                
                const response = await fetch(`/restapi/applications/${applicationId}/approve`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                });
                
                const data = await response.json();
                
                if (response.ok) {
                    showToast(data.message);
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה באישור המועמדות", "danger");
            }
        });
    }
    
    const rejectForm = document.querySelector('.reject-application-form');
    if (rejectForm) {
        rejectForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const applicationId = rejectForm.dataset.applicationId;
            
            try {
                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                
                const response = await fetch(`/restapi/applications/${applicationId}/reject`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        [csrfHeader]: csrfToken
                    }
                });
                
                const data = await response.json();
                
                if (response.ok) {
                    showToast(data.message);
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בדחיית המועמדות", "danger");
            }
        });
    }
}); */

// import {showToast} from "./toastUtils.js";
//
// const interviewForm = () => {
//     document.addEventListener('DOMContentLoaded', function () {
//         const toggleLocationField = (isVirtual, locationLabelId, locationInputId) => {
//             const locationLabel = document.getElementById(locationLabelId);
//             const locationInput = document.getElementById(locationInputId);
//
//             if (isVirtual) {
//                 locationLabel.textContent = 'קישור לפגישה';
//                 locationInput.placeholder = 'קישור לשיחת וידאו ייווצר אוטומטית';
//                 locationInput.value = '';
//                 locationInput.readOnly = true;
//                 locationInput.classList.add('bg-light');
//             } else {
//                 locationLabel.textContent = 'מיקום';
//                 locationInput.placeholder = 'מיקום פיזי';
//                 locationInput.readOnly = false;
//                 locationInput.classList.remove('bg-light');
//             }
//         };
//
//         const toggleMeetingType = () => {
//             const isVirtual = document.getElementById('meetingTypeSwitch').checked;
//             toggleLocationField(isVirtual, 'locationLabel', 'locationInput');
//             clearFieldError('locationInput');
//
//         };
//
//         const toggleEditMeetingType = () => {
//             const isVirtual = document.getElementById('editMeetingTypeSwitch').checked;
//             toggleLocationField(isVirtual, 'editLocationLabel', 'editLocationInput');
//             clearFieldError('editLocationInput');
//         };
//
//         /*const openJitsiLink = (link) => {
//             window.open(link, '_blank', 'width=1200,height=800');
//         };*/
//
//         const editInterview = (interviewId, date, location, notes, isVirtual) => {
//             const form = document.getElementById('editInterviewForm');
//             form.setAttribute('data-interview-id', interviewId);
//
//             document.getElementById('editInterviewDate').value = date;
//             document.getElementById('editLocationInput').value = location;
//             document.getElementById('editInterviewNotes').value = notes;
//             document.getElementById('editMeetingTypeSwitch').checked = isVirtual === 'true';
//
//             toggleLocationField(isVirtual === 'true', 'editLocationLabel', 'editLocationInput');
//
//             const modal = new bootstrap.Modal(document.getElementById('editInterviewModal'));
//             modal.show();
//         };
//
//         const showFieldError = (fieldId, message) => {
//             const field = document.getElementById(fieldId);
//             const errorDiv = document.getElementById(`${fieldId}-error`);
//
//             if (field && errorDiv) {
//                 field.classList.add('is-invalid');
//                 errorDiv.textContent = message;
//             }
//         };
//
//         const clearFieldError = (fieldId) => {
//             const field = document.getElementById(fieldId);
//             const errorDiv = document.getElementById(`${fieldId}-error`);
//
//             if (field) {
//                 field.classList.remove('is-invalid');
//             }
//             if (errorDiv) {
//                 errorDiv.textContent = '';
//             }
//         };
//
//         const clearAllErrors = (formId) => {
//             const form = document.getElementById(formId);
//             if (!form) return;
//
//             const fields = form.querySelectorAll('.is-invalid');
//             fields.forEach(field => {
//                 field.classList.remove('is-invalid');
//             });
//
//             const errorDivs = form.querySelectorAll('.invalid-feedback');
//             errorDivs.forEach(div => {
//                 div.textContent = '';
//             });
//         };
//
//         const validateInterviewForm = (data, formId) => {
//             let isValid = true;
//
//             clearAllErrors(formId);
//
//             const dateFieldId = formId === 'editInterviewForm' ? 'editInterviewDate' : 'interviewDate';
//             if (!data.interviewDate) {
//                 showFieldError(dateFieldId, 'חובה לבחור תאריך ושעה לראיון');
//                 isValid = false;
//             } else if (new Date(data.interviewDate) < new Date()) {
//                 showFieldError(dateFieldId, 'לא ניתן לקבוע ראיון בזמן שחלף');
//                 isValid = false;
//             }
//
//             const locationFieldId = formId === 'editInterviewForm' ? 'editLocationInput' : 'locationInput';
//             if (!data.isVirtual && (!data.location || data.location.trim() === '')) {
//                 showFieldError(locationFieldId, 'חובה להזין מיקום לפגישה פיזית או לבחור בפגישה וירטואלית');
//                 isValid = false;
//             }
//
//             return isValid;
//         };
//
//         const now = new Date();
//         const nowString = now.toISOString().slice(0, 16);
//
//         const dateInputs = document.querySelectorAll('input[type="datetime-local"]');
//         dateInputs.forEach(input => {
//             input.min = nowString;
//         });
//
//         const meetingTypeSwitch = document.getElementById('meetingTypeSwitch');
//         if (meetingTypeSwitch) {
//             meetingTypeSwitch.addEventListener('change', toggleMeetingType);
//         }
//
//         const editMeetingTypeSwitch = document.getElementById('editMeetingTypeSwitch');
//         if (editMeetingTypeSwitch) {
//             editMeetingTypeSwitch.addEventListener('change', toggleEditMeetingType);
//         }
//
//         const scheduleForm = document.querySelector('form[action="/interviews/schedule"]');
//         if (scheduleForm) {
//             scheduleForm.addEventListener('submit', function(e) {
//                 e.preventDefault();
//
//                 const formData = new FormData(this);
//                 const data = {
//                     applicationId: formData.get('applicationId'),
//                     interviewDate: formData.get('interviewDate'),
//                     location: formData.get('location'),
//                     notes: formData.get('notes'),
//                     isVirtual: formData.get('isVirtual') === 'on'
//                 };
//
//                 if (!validateInterviewForm(data, 'scheduleInterviewForm')) {
//                     return;
//                 }
//
//                 fetch('/restapi/interviews/schedule', {
//                     method: 'POST',
//                     headers: {
//                         'Content-Type': 'application/json',
//                         'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
//                     },
//                     body: JSON.stringify(data)
//                 })
//                     .then(response => {
//                         if (!response.ok) {
//                             return response.json().then(errorData => {
//                                 throw new Error(errorData.message || 'אירעה שגיאה בקביעת הראיון');
//                             });
//                         }
//                         return response.json();
//                     })
//                     .then(result => {
//                         if (result.success) {
//                             showToast(result.message);
//                             //setTimeout(() => window.location.reload(), 1500);
//                         } else {
//                             showToast(result.message, "danger");
//                         }
//                     })
//                     .catch(error => {
//                         showToast(error.message || 'אירעה שגיאה בקביעת הראיון', "danger");
//                     });
//             });
//         }
//
//         const editForm = document.getElementById('editInterviewForm');
//         if (editForm) {
//             editForm.addEventListener('submit', function(e) {
//                 e.preventDefault();
//
//                 const formData = new FormData(this);
//                 const interviewId = this.getAttribute('data-interview-id');
//                 const data = {
//                     interviewDate: formData.get('interviewDate'),
//                     location: formData.get('location'),
//                     notes: formData.get('notes'),
//                     isVirtual: formData.get('isVirtual') === 'on'
//                 };
//
//                 if (!validateInterviewForm(data, 'editInterviewForm')) {
//                     return;
//                 }
//
//                 fetch(`/restapi/interviews/${interviewId}/edit`, {
//                     method: 'POST',
//                     headers: {
//                         'Content-Type': 'application/json',
//                         'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
//                     },
//                     body: JSON.stringify(data)
//                 })
//                     .then(response => {
//                         if (!response.ok) {
//                             return response.json().then(errorData => {
//                                 throw new Error(errorData.message || 'אירעה שגיאה בעדכון הראיון');
//                             });
//                         }
//                         return response.json();
//                     })
//                     .then(result => {
//                         if (result.success) {
//                             showToast(result.message);
//                             const modal = bootstrap.Modal.getInstance(document.getElementById('editInterviewModal'));
//                             modal.hide();
//                             //setTimeout(() => window.location.reload(), 1500);
//                         } else {
//                             showToast(result.message, "danger");
//                         }
//                     })
//                     .catch(error => {
//                         showToast(error.message || 'אירעה שגיאה בעדכון הראיון', "danger");
//                     });
//             });
//         }
//
//         document.body.addEventListener('click', function (e) {
//             const btn = e.target.closest('.edit-interview-btn');
//             if (btn) {
//                 const id = btn.getAttribute('data-id');
//                 const date = btn.getAttribute('data-date');
//                 const location = btn.getAttribute('data-location');
//                 const notes = btn.getAttribute('data-notes');
//                 const isVirtual = btn.getAttribute('data-virtual');
//                 editInterview(id, date, location, notes, isVirtual);
//             }
//         });
//
//         const approveForm = document.querySelector('.approve-application-form');
//         if (approveForm) {
//             approveForm.addEventListener('submit', async (e) => {
//                 e.preventDefault();
//                 const applicationId = approveForm.dataset.applicationId;
//
//                 try {
//                     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
//                     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
//
//                     const response = await fetch(`/restapi/applications/${applicationId}/approve`, {
//                         method: 'POST',
//                         headers: {
//                             'Content-Type': 'application/json',
//                             [csrfHeader]: csrfToken
//                         }
//                     });
//
//                     const data = await response.json();
//
//                     if (response.ok) {
//                         showToast(data.message);
//                         setTimeout(() => window.location.reload(), 1500);
//                     } else {
//                         showToast(data.message, "danger");
//                     }
//                 } catch (error) {
//                     showToast("אירעה שגיאה באישור המועמדות", "danger");
//                 }
//             });
//         }
//
//         const rejectForm = document.querySelector('.reject-application-form');
//         if (rejectForm) {
//             rejectForm.addEventListener('submit', async (e) => {
//                 e.preventDefault();
//                 const applicationId = rejectForm.dataset.applicationId;
//
//                 try {
//                     const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
//                     const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
//
//                     const response = await fetch(`/restapi/applications/${applicationId}/reject`, {
//                         method: 'POST',
//                         headers: {
//                             'Content-Type': 'application/json',
//                             [csrfHeader]: csrfToken
//                         }
//                     });
//
//                     const data = await response.json();
//
//                     if (response.ok) {
//                         showToast(data.message);
//                         setTimeout(() => window.location.reload(), 1500);
//                     } else {
//                         showToast(data.message, "danger");
//                     }
//                 } catch (error) {
//                     showToast("אירעה שגיאה בדחיית המועמדות", "danger");
//                 }
//             });
//         }
//     });
// };
//
// interviewForm();