/**
 * Toast utilities module for displaying notification messages
 * @module toastUtils
 */

/**
 * Initializes toast functionality when DOM is loaded
 * Shows any existing toast elements on page load
 */
const toastUtils = () => {
    document.addEventListener("DOMContentLoaded", ()=>{
        const toastEl = document.getElementById('actionToast');
        if (toastEl) {
            const toast = new bootstrap.Toast(toastEl);
            toast.show();
        }
    });
};

/**
 * Displays a toast notification message
 * @param {string} message - The message to display in the toast
 * @param {string} type - The type of toast (success, danger, warning, info). Defaults to "success"
 */
export function showToast(message, type = "success") {
    const toastContainerId = "globalToastContainer";

    // Create toast container if it doesn't exist
    let container = document.getElementById(toastContainerId);
    if (!container) {
        container = document.createElement("div");
        container.id = toastContainerId;
        container.className = "toast-container position-fixed bottom-0 end-0 p-3";
        document.body.appendChild(container);
    }

    // Create the toast element
    const toast = document.createElement("div");
    toast.className = `toast align-items-center text-white bg-${type} border-0 show mb-2`;
    toast.setAttribute("role", "alert");
    toast.setAttribute("aria-live", "assertive");
    toast.setAttribute("aria-atomic", "true");

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    container.appendChild(toast);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        toast.remove();
    }, 5000);
}

toastUtils();