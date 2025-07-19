document.addEventListener("DOMContentLoaded", () => {
    const toastElement = document.getElementById("successToast");

    if (toastElement) {
        const toast = new bootstrap.Toast(toastElement, {
            delay: 5000,     // 5 seconds
            autohide: true   // Auto-dismiss
        });
        toast.show();
    }

});
