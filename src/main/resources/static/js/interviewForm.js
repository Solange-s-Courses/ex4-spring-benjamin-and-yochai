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
                window.showToast('לא ניתן לקבוע ראיון בזמן שחלף', true);
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
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    window.showToast(result.message);
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    window.showToast(result.message, true);
                }
            })
            .catch(error => {
                console.error('Error:', error);
                window.showToast('אירעה שגיאה בקביעת הראיון', true);
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
                window.showToast('לא ניתן לקבוע ראיון בזמן שחלף', true);
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
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    window.showToast(result.message);
                    const modal = bootstrap.Modal.getInstance(document.getElementById('editInterviewModal'));
                    modal.hide();
                    setTimeout(() => window.location.reload(), 1500);
                } else {
                    window.showToast(result.message, true);
                }
            })
            .catch(error => {
                window.showToast('אירעה שגיאה בעדכון הראיון', true);
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

    const editMeetingTypeSwitch = document.getElementById('editMeetingTypeSwitch');
    if (editMeetingTypeSwitch) {
        editMeetingTypeSwitch.addEventListener('change', function() {
            const isVirtual = this.checked;
            const locationLabel = document.getElementById('editLocationLabel');
            const locationInput = document.getElementById('editLocationInput');
            
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
        });
    }

    document.body.addEventListener('submit', function (e) {
        const form = e.target.closest('.cancel-interview-form');
        if (form && !confirm('האם אתה בטוח שברצונך לבטל את הראיון?')) {
            e.preventDefault();
        }
    });
}); 