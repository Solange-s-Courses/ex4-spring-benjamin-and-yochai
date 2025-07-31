/*window.pendingToasts = [];
window.isPollingActive = false;

function checkIfPollingActive() {
    return window.isPollingActive || 
           (typeof pollingInterval !== 'undefined' && pollingInterval !== null);
}

window.showToast = function(message, isError = false) {
    if (checkIfPollingActive()) {
        window.pendingToasts.push({
            message: message,
            isError: isError,
            timestamp: Date.now()
        });
        
        setTimeout(() => {
            if (!checkIfPollingActive()) {
                displayPendingToasts();
            }
        }, 1000);
    } else {
        displayToast(message, isError);
    }
};

function displayPendingToasts() {
    if (window.pendingToasts.length === 0) return;
    
    const toastData = window.pendingToasts.pop();
    window.pendingToasts = [];
    
    displayToast(toastData.message, toastData.isError);
}

function displayToast(message, isError) {
    let toastEl = document.getElementById("actionToast");
    
    if (!toastEl) {
        const toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        toastContainer.innerHTML = `
            <div id="actionToast" class="toast" role="alert" aria-live="assertive" aria-atomic="true">
                <div class="toast-header">
                    <strong class="me-auto">עדכון</strong>
                    <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
                </div>
                <div class="toast-body"></div>
            </div>
        `;
        document.body.appendChild(toastContainer);
        toastEl = document.getElementById("actionToast");
    }
    
    const toastBody = toastEl?.querySelector(".toast-body");
    
    if (!toastEl || !toastBody) {
        console.error('Toast elements not found');
        return;
    }
    
    toastBody.textContent = message;
    toastBody.className = isError ? "toast-body text-danger" : "toast-body";

    const toast = new bootstrap.Toast(toastEl, {
        delay: 4000,
        autohide: true
    });
    
    toast.show();
}

window.onPollingComplete = function() {
    window.isPollingActive = false;
    displayPendingToasts();
};

window.startPolling = function() {
    window.isPollingActive = true;
};

document.addEventListener("DOMContentLoaded", () => {
    const actionToast = document.getElementById("actionToast");
    if (actionToast) {
        const toast = new bootstrap.Toast(actionToast, {
            delay: 4000,
            autohide: true
        });
        toast.show();
    }

    const successToast = document.getElementById("successToast");
    if (successToast) {
        const toast = new bootstrap.Toast(successToast, {
            delay: 5000,
            autohide: true
        });
        toast.show();
    }

    const errorToast = document.getElementById("errorToast");
    if (errorToast) {
        const toast = new bootstrap.Toast(errorToast, {
            delay: 5000,
            autohide: true
        });
        toast.show();
    }
});*/
