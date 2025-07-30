import {showToast} from "./toastUtils.js";
import java.util.Arrays;
import java.util.stream.Collectors;

function toggleMeetingType() {
    const isVirtual = document.getElementById('meetingTypeSwitch').checked;
    const locationLabel = document.getElementById('locationLabel');
    const locationInput = document.getElementById('locationInput');
    
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

window.toggleMeetingType = toggleMeetingType;

function openJitsiLink(link) {
    window.open(link, '_blank', 'width=1200,height=800');
} 

function editInterview(interviewId, date, location, notes, isVirtual) {
    const form = document.getElementById('editInterviewForm');
    form.setAttribute('data-interview-id', interviewId);

    document.getElementById('editInterviewDate').value = date || '';
    document.getElementById('editLocationInput').value = location || '';
    document.getElementById('editInterviewNotes').value = notes || '';
    document.getElementById('editMeetingTypeSwitch').checked = isVirtual === 'true';
    
    const locationLabel = document.getElementById('editLocationLabel');
    const locationInput = document.getElementById('editLocationInput');
    
    if (isVirtual === 'true') {
        locationLabel.textContent = 'קישור לפגישה';
        locationInput.placeholder = 'קישור לשיחת וידאו ייווצר אוטומטית';
        locationInput.readOnly = true;
        locationInput.classList.add('bg-light');
    } else {
        locationLabel.textContent = 'מיקום';
        locationInput.placeholder = 'מיקום פיזי';
        locationInput.readOnly = false;
        locationInput.classList.remove('bg-light');
    }

    const modal = new bootstrap.Modal(document.getElementById('editInterviewModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', function () {
    const now = new Date();
    const nowString = now.toISOString().slice(0, 16);
    
    const dateInputs = document.querySelectorAll('input[type="datetime-local"]');
    dateInputs.forEach(input => {
        input.min = nowString;
    });
    
    // הוספת event listener ל-meetingTypeSwitch
    const meetingTypeSwitch = document.getElementById('meetingTypeSwitch');
    if (meetingTypeSwitch) {
        meetingTypeSwitch.addEventListener('change', toggleMeetingType);
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
            
            if (data.interviewDate && new Date(data.interviewDate) < new Date()) {
                showToast('לא ניתן לקבוע ראיון בזמן שחלף', "danger");
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
            
            if (data.interviewDate && new Date(data.interviewDate) < new Date()) {
                showToast('לא ניתן לקבוע ראיון בזמן שחלף', "danger");
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
}); 