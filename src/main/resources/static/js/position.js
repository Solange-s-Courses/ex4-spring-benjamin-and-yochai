document.addEventListener("DOMContentLoaded", () => {
    const getCSRFHeaders = () => {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const headers = { 'Content-Type': 'application/json' };
        
        if (csrfMeta) {
            const csrfToken = csrfMeta.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            headers[csrfHeader] = csrfToken;
        }
        
        return headers;
    };

    const setButtonLoading = (button, loadingText) => {
        button.dataset.originalText = button.innerHTML;
        button.innerHTML = `<i class="bi bi-hourglass-split me-2"></i>${loadingText}`;
        button.disabled = true;
    };

    const resetButton = (button) => {
        button.innerHTML = button.dataset.originalText;
        button.disabled = false;
    };

    const handleFormSubmission = async (url, options = {}) => {
        const {
            loadingText = 'שולח...',
            fallbackErrorMessage = 'אירעה שגיאה',
            reloadDelay = 1500,
            body = null,
            button = null
        } = options;

        if (button) {
            setButtonLoading(button, loadingText);
        }

        try {
            const headers = getCSRFHeaders();
            const requestOptions = {
                method: 'POST',
                headers,
                credentials: 'include'
            };

            if (body) {
                requestOptions.body = JSON.stringify(body);
            }

            const response = await fetch(url, requestOptions);
            const data = await response.json();

            if (response.ok) {
                if (data.message) {
                    showToast(data.message, false);
                }
                setTimeout(() => window.location.reload(), reloadDelay);
            } else {
                const errorMessage = data.message || fallbackErrorMessage;
                showToast(errorMessage, true);
                if (button) {
                    resetButton(button);
                }
            }
        } catch (error) {
            showToast(fallbackErrorMessage, true);
            if (button) {
                resetButton(button);
            }
        }
    };

    const initShareButton = () => {
        const shareBtn = document.getElementById("shareBtn");
        if (!shareBtn) return;

        shareBtn.addEventListener("click", () => {
            if (navigator.share) {
                navigator.share({
                    title: document.querySelector('h2')?.textContent || '',
                    text: 'בדוק את המשרה הזו:',
                    url: window.location.href
                }).catch(() => {
                    navigator.clipboard.writeText(window.location.href).then(() => {
                        const messageBox = document.getElementById('copy-message');
                        if (messageBox) {
                            messageBox.classList.remove("d-none");
                            setTimeout(() => messageBox.classList.add('d-none'), 3000);
                        }
                    });
                });
            } else {
                navigator.clipboard.writeText(window.location.href).then(() => {
                    const messageBox = document.getElementById('copy-message');
                    if (messageBox) {
                        messageBox.classList.remove("d-none");
                        setTimeout(() => messageBox.classList.add('d-none'), 3000);
                    }
                });
            }
        });
    };

    const initFormHandlers = () => {
        // Status change forms
        const statusForms = document.querySelectorAll('form[action*="/positions/"][action*="/status"]');
        statusForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const statusSelect = form.querySelector('select[name="status"]');
                if (!statusSelect) return;
                
                const positionId = form.action.split('/positions/')[1].split('/status')[0];
                const button = form.querySelector('button[type="submit"]');
                
                handleFormSubmission(`/restapi/positions/${positionId}/status`, {
                    loadingText: 'מעדכן...',
                    fallbackErrorMessage: 'אירעה שגיאה בעדכון סטטוס המשרה.',
                    body: { status: statusSelect.value },
                    reloadDelay: 1500,
                    button: button
                });
            });
        });

        // Apply form
        const applyForm = document.querySelector('.apply-form');
        if (applyForm) {
            applyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const button = applyForm.querySelector('button');
                
                handleFormSubmission(applyForm.action, {
                    loadingText: 'מגיש...',
                    fallbackErrorMessage: 'שגיאה בלתי צפויה.',
                    reloadDelay: 4000,
                    button: button
                });
            });
        }

        // Cancel forms
        const cancelForms = document.querySelectorAll('form[action*="/restapi/"][action*="/cancel"]');
        cancelForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const button = form.querySelector('button[type="submit"]');
                
                handleFormSubmission(form.action, {
                    loadingText: 'מבטל...',
                    fallbackErrorMessage: 'אירעה שגיאה בביטול המועמדות.',
                    reloadDelay: 1500,
                    button: button
                });
            });
        });
    };

    initShareButton();
    initFormHandlers();
});