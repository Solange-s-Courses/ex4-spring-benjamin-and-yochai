
const positionsPageDom = ()=>{
    document.addEventListener('DOMContentLoaded', function () {
        const urlParams = new URLSearchParams(window.location.search);

        if (urlParams.has('success')) {
            const successToast = new bootstrap.Toast(document.getElementById('successToast'), {
                autohide: true,
                delay: 5000 // 5 seconds
            });

            successToast.show();

            // Clean up URL (optional)
            const newUrl = window.location.pathname;
            window.history.replaceState({}, document.title, newUrl);
        }
    });
};

positionsPageDom();

// here we need to add filtration, and sorting