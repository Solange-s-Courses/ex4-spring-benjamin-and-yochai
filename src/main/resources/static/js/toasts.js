document.addEventListener("DOMContentLoaded", () => {
    const toastElements = document.querySelectorAll('.toast');

    toastElements.forEach((toastEl) => {
        const toast = new bootstrap.Toast(toastEl, {
            delay: 5000,     // 5 seconds
            autohide: true   // Auto-dismiss
        });
        toast.show();
    });
});
