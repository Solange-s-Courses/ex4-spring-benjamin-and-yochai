const PositionDom = ()=>{
    document.addEventListener("DOMContentLoaded",()=>{
        // Share button functionality
        document.getElementById("shareBtn").addEventListener("click",()=>{
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

        // Show success/error messages
        const successMessage = document.querySelector('[data-th-if="${successMessage}"]');
        const errorMessage = document.querySelector('[data-th-if="${errorMessage}"]');
        
        if (successMessage) {
            showToast('success', successMessage.textContent);
        }
        
        if (errorMessage) {
            showToast('error', errorMessage.textContent);
        }
    });
}

function showToast(type, message) {
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) return;

    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : 'danger'} border-0`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                ${message}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
    `;

    toastContainer.appendChild(toast);
    
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
}

PositionDom();