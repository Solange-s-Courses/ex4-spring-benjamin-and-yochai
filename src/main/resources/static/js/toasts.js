document.addEventListener("DOMContentLoaded", () => {
    // Show success toast
    const successToast = document.getElementById("successToast");
    if (successToast) {
        const toast = new bootstrap.Toast(successToast, {
            delay: 5000,     // 5 seconds
            autohide: true   // Auto-dismiss
        });
        toast.show();
    }

    // Show error toast
    const errorToast = document.getElementById("errorToast");
    if (errorToast) {
        const toast = new bootstrap.Toast(errorToast, {
            delay: 5000,     // 5 seconds
            autohide: true   // Auto-dismiss
        });
        toast.show();
    }
});
