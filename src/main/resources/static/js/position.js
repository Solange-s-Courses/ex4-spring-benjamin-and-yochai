const PositionDom = ()=>{
    document.addEventListener("DOMContentLoaded",()=>{
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
        })
    });
}

PositionDom();