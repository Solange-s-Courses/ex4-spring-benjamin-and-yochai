const renderPdfWithPdfJs = async (url, container) => {
    container.innerHTML = ''; // Clear old content

    // Load the PDF
    const loadingTask = pdfjsLib.getDocument(url);
    const pdf = await loadingTask.promise;

    // Render each page
    for (let i = 1; i <= pdf.numPages; i++) {
        const page = await pdf.getPage(i);

        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        const viewport = page.getViewport({ scale: 3 });

        canvas.width = viewport.width;
        canvas.height = viewport.height;
        canvas.style.maxWidth = "100%";
        canvas.style.height = "auto";

        const border = document.createElement("div");
        border.classList.add("alert", "alert-dark", "mt-3");
        border.role = "alert";

        //border.appendChild(canvas);
        //container.appendChild(border); // Add canvas to modal
        container.appendChild(canvas);

        await page.render({
            canvasContext: context,
            viewport: viewport
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

        buttons.forEach(button => {
            button.addEventListener("click", async function () {
                const userId = this.dataset.userId;
                const fullName = this.dataset.userFullName;
                const url = `/restapi/admin/document/${userId}`;

                modalOpenNewPageBtn.href = url;
                spinner.classList.remove("d-none");
                pdfContainer.classList.add("d-none");
                nameDisplay.innerText = fullName;

                try {
                    await renderPdfWithPdfJs(url, pdfContainer);
                } catch (err) {
                    pdfContainer.innerHTML = `<p class="text-danger">${err.message}</p>`;
                } finally {
                    spinner.classList.add("d-none");
                    pdfContainer.classList.remove("d-none");
                }
                // const userId = this.dataset.userId;
                // const fullName = this.dataset.userFullName;
                //
                // // Show spinner
                // spinner.classList.remove("d-none");
                //
                // nameDisplay.innerText = fullName;
                //
                // const frame = document.createElement("iframe");
                // frame.id = 'pdf-frame';
                // frame.type = 'application/pdf';
                // frame.width = '100%';
                // frame.height = '600px';
                // frame.src = `/restapi/admin/document/${userId}`;
                // frame.classList.add("d-none")
                //
                // modalBody.appendChild(frame);
                //
                // frame.addEventListener("load", () => {
                //     spinner.classList.add("d-none");
                //     frame.classList.remove("d-none");
                // });
                //
                // setTimeout(() => {
                //     if (!frame.classList.contains("d-none")) return; // Already shown
                //     spinner.classList.add("d-none");
                //     frame.classList.remove("d-none");
                //     console.log("timeout")
                // }, 5000);
            });
        })

        // document.getElementById("pdfModal").addEventListener("hidden.bs.modal", ()=>{
        //     const frame = document.getElementById("pdf-frame");
        //     modalBody.removeChild(frame);
        // })
    })
};

adminDashboard();