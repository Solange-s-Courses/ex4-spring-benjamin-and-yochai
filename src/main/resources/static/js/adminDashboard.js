
const adminDashboard = ()=>{
    const renderPdfWithPdfJs = async (url, container) => {
        pdfjsLib.GlobalWorkerOptions.workerSrc = 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js';
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

    const filterTable = (table, searchValue)=>{
        const rows = table.querySelectorAll("tr[data-username]");
        const noUsersMsg = table.querySelector("tr[data-error='no-users']");

        noUsersMsg.classList.add('d-none');
        const searchTerm = searchValue.toLowerCase().trim();
        let counter = 0;

        rows.forEach(row => {
            const username = row.dataset.username.toLowerCase();

            if (searchTerm === '' || username.includes(searchTerm)) {
                row.classList.remove('d-none');
                counter++;
            } else {
                row.classList.add('d-none');
            }
        });
        if (counter === 0){
            noUsersMsg.classList.remove('d-none');
        }
    }

    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    }

    document.addEventListener("DOMContentLoaded", function () {
        const showPdfButtons = document.querySelectorAll(".view-pdf-btn");
        const spinner = document.getElementById("pdf-spinner");
        const modalOpenNewPageBtn = document.getElementById("modalOpenBtn");
        const nameDisplay = document.getElementById("modal-fullName");
        const pdfContainer = document.getElementById("pdf-container");
        const pdfModal = document.getElementById('pdfModal');
        const newAccountsTable = document.getElementById("newAccountsTbody");
        const newAccountsSearch = document.getElementById("newAccSearch");
        const permissionTable = document.getElementById("accountsPermissionTbody");
        const permissionSearch = document.getElementById("accSearch");
        const setStatusButtons = newAccountsTable.querySelectorAll(".status-form");
        const changeStatusForms = permissionTable.querySelectorAll(".change-status-form");
        const changeRoleForms = permissionTable.querySelectorAll(".change-role-form");
        const toastEl = document.getElementById("actionToast");
        const toastBody = toastEl.querySelector(".toast-body");

        permissionSearch.addEventListener("input", ()=>{filterTable(permissionTable, permissionSearch.value)});
        newAccountsSearch.addEventListener("input", ()=>{filterTable(newAccountsTable, newAccountsSearch.value)});

        filterTable(permissionTable, permissionSearch.value);
        filterTable(newAccountsTable, newAccountsSearch.value);

        showPdfButtons.forEach(button => {
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
                } catch (err) {
                    pdfContainer.innerHTML = `<p class="text-danger">${err.message}</p>`;
                } finally {
                    spinner.classList.add("d-none");
                    pdfContainer.classList.remove("d-none");
                }
            });
        })

        setStatusButtons.forEach(form => {
            form.addEventListener("submit", async (e) => {
                e.preventDefault();

                const userId = form.dataset.userId;
                const newStatus = form.dataset.newStatus;

                const ok = await changeUserStatus(userId, newStatus);
                if (ok){
                    form.closest("tr").remove();
                    filterTable(newAccountsTable, newAccountsSearch.value);
                }
            });
        });

        changeStatusForms.forEach(form => {
            form.addEventListener("submit", async (e)=> {
                e.preventDefault();

                const userId = form.querySelector("input").value;
                const selector = form.querySelector("select");
                const newStatus = selector.value;
                const oldStatus = selector.dataset.status;

                if (newStatus !== oldStatus){
                    const ok = await changeUserStatus(userId, newStatus);
                    if (ok){
                        selector.dataset.status = newStatus;
                    }
                }
            });
        });

        changeRoleForms.forEach(form => {
            form.addEventListener("submit", async (e)=> {
                e.preventDefault();

                const userId = form.querySelector("input").value;
                const selector = form.querySelector("select");
                const newRole = selector.value;
                const oldRole = selector.dataset.role;

                if (newRole !== oldRole){
                    const ok = await changeUserRole(userId, newRole);
                    if (ok){
                        selector.dataset.role = newRole;
                    }
                }
            });
        });

        async function changeUserStatus(userId, status){
            let response = {};
            try {

                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                response = await fetch("/restapi/admin/changeUserStatus", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ userId, status }),
                    credentials: "include",
                });

                if (response.ok) {
                    showToast("הסטטוס עודכן בהצלחה.");
                } else {
                    const text = await response.text();
                    showToast("שגיאה בעדכון הסטטוס: " + text, true);
                }
            } catch (err) {
                showToast("שגיאה בלתי צפויה.", true);
            }
            return response.ok;
        }

        async function changeUserRole(userId, role){
            let response = {};
            try {

                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                response = await fetch("/restapi/admin/changeUserRole", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ userId, role }),
                    credentials: "include",
                });

                if (response.ok) {
                    showToast("התפקיד עודכן בהצלחה.");
                } else {
                    const text = await response.text();
                    showToast("שגיאה בעדכון התפקיד: " + text, true);
                }
            } catch (err) {
                showToast("שגיאה בלתי צפויה.", true);
            }
            return response.ok;
        }

        function showToast(message, isError = false) {
            toastBody.textContent = message;

            if (isError) {
                toastBody.classList.add("text-danger");
            } else {
                toastBody.classList.remove("text-danger");
            }

            const toast = new bootstrap.Toast(toastEl, {
                delay: 4000,
                autohide: true
            });
            toast.show();
        }

        pdfModal.addEventListener('hide.bs.modal', function() {
            // Remove focus from close button before hiding
            const focusedElement = this.querySelector(':focus');
            if (focusedElement) {
                focusedElement.blur();
            }
        });

    })
};

adminDashboard();