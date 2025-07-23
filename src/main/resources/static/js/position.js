const PositionDom = ()=>{
    document.addEventListener("DOMContentLoaded",()=>{
        // Share button functionality
        const shareBtn = document.getElementById("shareBtn");
        if (shareBtn) {
            shareBtn.addEventListener("click",()=>{
                if (navigator.share) {
                    navigator.share({
                        title: document.querySelector('h2').textContent,
                        text: 'בדוק את המשרה הזו:',
                        url: window.location.href
                    });
                } else {
                    // Fallback - copy URL to clipboard
                    navigator.clipboard.writeText(window.location.href).then(function () {
                        const messageBox = document.getElementById('copy-message');
                        messageBox.classList.remove("d-none");

                        setTimeout(() => {
                            messageBox.classList.add('d-none');
                        }, 3000);
                    });
                }
            });
        }

        // Handle position status change form submission
        const statusForms = document.querySelectorAll('form[action*="/positions/"][action*="/status"]');
        
        statusForms.forEach(form => {
            form.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const button = this.querySelector('button[type="submit"]');
                const originalText = button.innerHTML;
                button.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>מעדכן...';
                button.disabled = true;
                
                try {
                    const positionId = this.action.split('/positions/')[1].split('/status')[0];
                    const statusSelect = this.querySelector('select[name="status"]');
                    const newStatus = statusSelect.value;
                    
                    // בדיקה אם יש CSRF token בעמוד
                    const csrfMeta = document.querySelector('meta[name="_csrf"]');
                    const headers = {
                        'Content-Type': 'application/json'
                    };
                    
                    if (csrfMeta) {
                        const csrfToken = csrfMeta.getAttribute('content');
                        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                        headers[csrfHeader] = csrfToken;
                    }
                    
                    const response = await fetch(`/restapi/positions/${positionId}/status`, {
                        method: 'POST',
                        headers: headers,
                        body: JSON.stringify({ status: newStatus }),
                        credentials: 'include'
                    });
                    
                    const data = await response.json();
                    
                    if (response.ok) {
                        showToast(data.message, false);
                        setTimeout(() => {
                            window.location.reload();
                        }, 1500);
                    } else {
                        showToast(data.message, true);
                        button.innerHTML = originalText;
                        button.disabled = false;
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showToast('אירעה שגיאה בעדכון סטטוס המשרה.', true);
                    button.innerHTML = originalText;
                    button.disabled = false;
                }
            });
        });

        const applyForm = document.querySelector('.apply-form');

        if (applyForm) {
            applyForm.addEventListener('submit', async function(e) {
                e.preventDefault();
                
                const button = this.querySelector('button');
                const originalText = button.innerHTML;
                button.innerHTML = '<i class="bi bi-hourglass-split me-2"></i>מגיש...';
                button.disabled = true;

                try {
                    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                    const headers = {
                        "Content-Type": "application/json",
                        [csrfHeader]: csrfToken
                    };

                    const response = await fetch(this.action, {
                        method: "POST",
                        headers: headers,
                        credentials: "include",
                    });

                    if (response.ok) {
                        const data = await response.json();
                        showToast(data.message || "המועמדות הוגשה בהצלחה!");
                        
                        // Increase the delay to 4 seconds to match toast duration
                        setTimeout(() => window.location.reload(), 4000);
                    } else {
                        const errorData = await response.json();
                        showToast(errorData.message || "שגיאה בהגשת המועמדות.", true);
                        
                        button.innerHTML = originalText;
                        button.disabled = false;
                    }
                } catch (error) {
                    console.error('Error:', error);
                    showToast("שגיאה בלתי צפויה.", true);
                    
                    button.innerHTML = originalText;
                    button.disabled = false;
                }
            });
        }

        // Handle cancel application form submission
        const cancelForms = document.querySelectorAll('form[action*="/restapi/"][action*="/cancel"]');
        
        cancelForms.forEach(form => {
            form.addEventListener('submit', function(e) {
                e.preventDefault();
                
                // בדיקה אם יש CSRF token בעמוד
                const csrfMeta = document.querySelector('meta[name="_csrf"]');
                const headers = {
                    'Content-Type': 'application/json'
                };
                
                if (csrfMeta) {
                    const csrfToken = csrfMeta.getAttribute('content');
                    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                    headers[csrfHeader] = csrfToken;
                }
                
                fetch(form.action, {
                    method: 'POST',
                    headers: headers,
                    credentials: 'include'
                })
                .then(response => {
                    return response.json().then(data => {
                        return { response, data };
                    });
                })
                .then(({ response, data }) => {
                    if (response.ok) {
                        showToast(data.message, false);
                        // בעמוד משרה - נשאר באותו עמוד
                        setTimeout(() => {
                            window.location.reload();
                        }, 1500);
                    } else {
                        showToast(data.message, true);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showToast('אירעה שגיאה בביטול המועמדות.', true);
                });
            });
        });
    });
}

PositionDom();