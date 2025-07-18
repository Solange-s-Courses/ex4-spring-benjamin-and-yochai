
const renderPdfWithPdfJs = async (url, container) => {
    //container.innerHTML = ''; // Clear old content

    // Load the PDF
    const loadingTask = pdfjsLib.getDocument(url);
    const pdf = await loadingTask.promise;

    // Render each page
    for (let i = 1; i <= pdf.numPages; i++) {
        const page = await pdf.getPage(i);

        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        const viewport = page.getViewport({ scale: 3 });

        context.font = "24px Arial";
        context.direction = "rtl";

        canvas.width = viewport.width * 2;
        canvas.height = viewport.height * 2;
        canvas.style.width = "100%";
        canvas.style.height = "auto";

        container.appendChild(canvas);

        await page.render({
            canvasContext: context,
            viewport: viewport,
            transform:[2,0,0,2,0,0]
        }).promise;
    }
};


const adminDashboard = ()=>{
    document.addEventListener("DOMContentLoaded", function () {
        const buttons = document.querySelectorAll(".view-pdf-btn");
        const spinner = document.getElementById("pdf-spinner");
        const modalOpenNewPageBtn = document.getElementById("modalOpenBtn");
        const nameDisplay = document.getElementById("modal-fullName");
        const pdfContainer = document.getElementById("pdf-container");

        const renderPdfWithViewer = async (url, container) => {
            //container.innerHTML = ''; // Clear old content

            // Create iframe with PDF.js viewer
            const iframe = document.createElement('iframe');
            //iframe.src = `https://mozilla.github.io/pdf.js/web/viewer.html?file=${encodeURIComponent(url)}`;
            iframe.src = `/assets/pdfjs/web/viewer.html?file=${encodeURIComponent(url)}`;
            iframe.style.width = '100%';
            iframe.style.height = '800px';
            iframe.style.border = 'none';
            iframe.style.borderRadius = '8px';
            iframe.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';

            container.appendChild(iframe);

            iframe.addEventListener("load", ()=>{
                spinner.classList.add("d-none");
                pdfContainer.classList.remove("d-none");
            })
        };

        buttons.forEach(button => {
            button.addEventListener("click", async function () {
                const userId = this.dataset.userId;
                const fullName = this.dataset.userFullName;
                const url = `/restapi/admin/document/${userId}`;

                modalOpenNewPageBtn.href = url;
                spinner.classList.remove("d-none");
                pdfContainer.classList.add("d-none");
                nameDisplay.innerText = fullName;

                pdfContainer.innerHTML = "";

                try {
                    await renderPdfWithPdfJs(url, pdfContainer);
                    //await renderPdfWithViewer(url, pdfContainer);
                } catch (err) {
                    pdfContainer.innerHTML = `<p class="text-danger">${err.message}</p>`;
                } finally {
                    spinner.classList.add("d-none");
                    pdfContainer.classList.remove("d-none");
                }
            });
        })


    })
};

adminDashboard();