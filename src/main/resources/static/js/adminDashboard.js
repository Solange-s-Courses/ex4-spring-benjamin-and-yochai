/**
 * Admin dashboard functionality module
 * @module adminDashboard
 */

import {showToast} from "./toastUtils.js";

/**
 * Initializes admin dashboard functionality including user management and PDF viewing
 */
const adminDashboard = ()=>{
    const POLLING = 5000;
    
    /**
     * Renders PDF document using PDF.js library
     * @param {string} url - URL of the PDF document
     * @param {HTMLElement} container - Container element to render PDF into
     */
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

    /**
     * Filters table rows based on search value
     * @param {HTMLTableElement} table - Table element to filter
     * @param {string} searchValue - Search term to filter by
     */
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

    document.addEventListener("DOMContentLoaded", function () {
        const showPdfButtons = document.querySelectorAll(".view-pdf-btn");
        const spinner = document.getElementById("pdf-spinner");
        const modalOpenNewPageBtn = document.getElementById("modalOpenBtn");
        const nameDisplay = document.getElementById("modal-fullName");
        const pdfContainer = document.getElementById("pdf-container");
        const newAccountsTable = document.getElementById("newAccountsTbody");
        const newAccountsSearch = document.getElementById("newAccSearch");
        const permissionTable = document.getElementById("accountsPermissionTbody");
        const permissionSearch = document.getElementById("accSearch");
        const setStatusButtons = newAccountsTable.querySelectorAll(".status-form");
        const changeStatusForms = permissionTable.querySelectorAll(".change-status-form");
        const changeRoleForms = permissionTable.querySelectorAll(".change-role-form");
        let pollingInterval = null;

        // Search functionality
        permissionSearch.addEventListener("input", ()=>{filterTable(permissionTable, permissionSearch.value)});
        newAccountsSearch.addEventListener("input", ()=>{filterTable(newAccountsTable, newAccountsSearch.value)});

        filterTable(permissionTable, permissionSearch.value);
        filterTable(newAccountsTable, newAccountsSearch.value);

        // PDF viewing functionality
        showPdfButtons.forEach(button => {
            button.addEventListener("click", async function () {
                await showPdfListener(button);
            });
        })

        /**
         * Handles PDF viewing button click
         * @param {HTMLButtonElement} button - Button that was clicked
         */
        async function showPdfListener (button){
            const userId = button.dataset.userId;
            const fullName = button.dataset.userFullName;
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
        }

        // Status change functionality for new accounts
        setStatusButtons.forEach(form => {
            form.addEventListener("submit", async (event) => {
                await setStatusListener(form, event);
            });
        });

        /**
         * Handles status change for new accounts
         * @param {HTMLFormElement} form - Form element
         * @param {Event} event - Form submit event
         */
        async function setStatusListener(form, event){
            clearInterval(pollingInterval);
            event.preventDefault();

            const userId = form.dataset.userId;
            const newStatus = form.dataset.newStatus;

            const updatedUser = await changeUserStatus(userId, newStatus);
            if (updatedUser){
                await refreshData();
            }
            pollingInterval = setInterval(refreshData, POLLING);
        }

        // Status change functionality for existing accounts
        changeStatusForms.forEach(form => {
            form.addEventListener("submit", async(event)=>{await changeStatusListener(form, event)});
        });

        /**
         * Handles status change for existing accounts
         * @param {HTMLFormElement} form - Form element
         * @param {Event} event - Form submit event
         */
        async function changeStatusListener(form, event){
            event.preventDefault();

            const userId = form.querySelector("input").value;
            const selector = form.querySelector("select");
            const newStatus = selector.value;
            const oldStatus = selector.dataset.status;

            if (newStatus !== oldStatus){
                clearInterval(pollingInterval);
                const updatedUser = await changeUserStatus(userId, newStatus);
                if (updatedUser){
                    selector.dataset.status = newStatus;
                    await refreshData();
                }
                pollingInterval = setInterval(refreshData, POLLING);
            }
        }

        // Role change functionality
        changeRoleForms.forEach(form => {
            form.addEventListener("submit", async (event)=> {await changeRoleListener(form, event)});
        });

        /**
         * Handles role change for users
         * @param {HTMLFormElement} form - Form element
         * @param {Event} event - Form submit event
         */
        async function changeRoleListener(form, event){
            event.preventDefault();

            const userId = form.querySelector("input").value;
            const selector = form.querySelector("select");
            const newRole = selector.value;
            const oldRole = selector.dataset.role;

            if (newRole !== oldRole){
                clearInterval(pollingInterval);
                const updatedUser = await changeUserRole(userId, newRole);
                if (updatedUser){
                    selector.dataset.role = newRole;
                    await refreshData();
                }
                pollingInterval = setInterval(refreshData, POLLING);
            }
        }

        /**
         * Updates permission table with user data
         * @param {Object} user - User data object
         */
        function updatePermissionTable(user) {
            const row = permissionTable.querySelector(`tr[data-username="${user.username}"]`);
            if (row) {
                // Update role select
                const roleSelect = row.querySelector('select[name="newRole"]');
                if (roleSelect.dataset.role !== user.role) {
                    roleSelect.value = user.role;
                    roleSelect.setAttribute('data-role', user.role);
                }

                // Update status select
                const statusSelect = row.querySelector('select[name="newStatus"]');
                if (statusSelect.dataset.status !== user.registrationStatus) {
                    statusSelect.value = user.registrationStatus;
                    statusSelect.setAttribute('data-status', user.registrationStatus);
                }
            }
            else{
                const newRow = document.createElement("tr");
                newRow.setAttribute("data-username", user.username);

                newRow.innerHTML = `
                    <td>${user.firstName} ${user.lastName}</td>
                    <td>${user.email}</td>
                    <td>${user.username}</td>
                    <td>
                        <form class="d-flex justify-content-center change-role-form">
                            <input type="hidden" name="userId" value="${user.id}">
                            <div class="input-group input-group-sm w-auto">
                                <select class="form-select" name="newRole" data-role="${user.role}">
                                    <option value="RESERVIST" ${user.role === 'RESERVIST' ? 'selected' : ''}>חייל</option>
                                    <option value="COMMANDER" ${user.role === 'COMMANDER' ? 'selected' : ''}>מפקד</option>
                                    <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>מנהל</option>
                                </select>
                                <button type="submit" class="btn btn-primary">שמור</button>
                            </div>
                        </form>
                    </td>
                    <td>
                        <form class="d-flex justify-content-center change-status-form">
                            <input type="hidden" name="userId" value="${user.id}">
                            <div class="input-group input-group-sm w-auto">
                                <select class="form-select" name="newStatus" data-status="${user.registrationStatus}">
                                    <option value="PENDING" ${user.registrationStatus === 'PENDING' ? 'selected' : ''}>ממתין לאישור</option>
                                    <option value="APPROVED" ${user.registrationStatus === 'APPROVED' ? 'selected' : ''}>פעיל</option>
                                    <option value="BLOCKED" ${user.registrationStatus === 'BLOCKED' ? 'selected' : ''}>חסום</option>
                                </select>
                                <button type="submit" class="btn btn-primary">שמור</button>
                            </div>
                        </form>
                    </td>
                `;

                permissionTable.appendChild(newRow);

                const statusForm = newRow.querySelector(".change-status-form");
                const roleForm = newRow.querySelector(".change-role-form");
                statusForm.addEventListener("submit", async (event)=> {await changeStatusListener(statusForm, event)});
                roleForm.addEventListener("submit", async (event)=> {await changeRoleListener(roleForm, event)});
            }
        }

        /**
         * Changes user status via API
         * @param {string} userId - User ID to change status for
         * @param {string} status - New status value
         * @returns {Object|null} Updated user data or null if failed
         */
        async function changeUserStatus(userId, status){
            try {

                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                const response = await fetch("/restapi/admin/changeUserStatus", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ userId, status }),
                    credentials: "include",
                });

                if (response.ok) {
                    showToast("הסטטוס עודכן בהצלחה.");
                    return await response.json();
                } else {
                    const text = await response.text();
                    showToast("שגיאה בעדכון הסטטוס: " + text, "danger");
                    return null;
                }
            } catch (err) {
                showToast("שגיאה בלתי צפויה.", "danger");
                return null;
            }

        }

        /**
         * Changes user role via API
         * @param {string} userId - User ID to change role for
         * @param {string} role - New role value
         * @returns {Object|null} Updated user data or null if failed
         */
        async function changeUserRole(userId, role){
            try {

                const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

                const headers = {
                    "Content-Type": "application/json",
                    [csrfHeader]: csrfToken
                };

                const response = await fetch("/restapi/admin/changeUserRole", {
                    method: "POST",
                    headers: headers,
                    body: JSON.stringify({ userId, role }),
                    credentials: "include",
                });

                if (response.ok) {
                    showToast("התפקיד עודכן בהצלחה.");
                    return await response.json();
                } else {
                    const text = await response.text();
                    showToast("שגיאה בעדכון התפקיד: " + text, "danger");
                    return null;
                }
            } catch (err) {
                showToast("שגיאה בלתי צפויה.", "danger");
                return null;
            }
        }

        /**
         * Fetches all users data from server
         * @returns {Object|null} Users data or null if failed
         */
        async function fetchAllUsers(){
            try {
                const response = await fetch('/restapi/admin/allUsers', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                return data;
            } catch (error) {
                console.error('Error fetching positions data:', error);
                return null;
            }
        }

        /**
         * Updates pending accounts table with user data
         * @param {Object} user - User data object
         */
        function updatePendingTable(user) {
            const row = newAccountsTable.querySelector(`tr[data-username="${user.username}"]`);
            if (!row && user.registrationStatus === "PENDING") {
                const newRow = document.createElement("tr");
                newRow.setAttribute("data-username", user.username);

                newRow.innerHTML = `
                    <td>${user.firstName + ' ' + user.lastName}</td>
                    <td>${user.username}</td>
                    <td>${user.about}</td>
                    <td class="role-column">${user.role}</td>
                    <td>
                        <button class="btn btn-outline-primary btn-sm view-pdf-btn"
                            data-user-id="${user.id}"
                            data-user-full-name="${user.firstName + ' ' + user.lastName}"
                            data-bs-toggle="modal"
                            data-bs-target="#pdfModal">
                                הצג תעודה
                        </button>
                    </td>
                    <td>    
                        <form class="status-form d-inline ms-2" data-user-id="${user.id}" data-new-status="BLOCKED">
                            <button type="submit" class="btn btn-danger btn-sm">חסום</button>
                        </form>
                        <form class="status-form d-inline ms-2" data-user-id="${user.id}" data-new-status="APPROVED">
                            <button type="submit" class="btn btn-success btn-sm">אשר</button>
                        </form>
                    </td>
                `;

                newAccountsTable.appendChild(newRow);

                const statusForms = newRow.querySelectorAll(".status-form");
                statusForms.forEach(form => {
                    form.addEventListener("submit", async (event) => {
                        await setStatusListener(form, event);
                    });
                });
                const pdfButton = newRow.querySelector(".view-pdf-btn");
                pdfButton.addEventListener("click", async()=>{
                    await showPdfListener(pdfButton);
                });
            }
            else if (row && user.registrationStatus !== "PENDING") {
                row.remove();
            }
            else if (row && user.registrationStatus === "PENDING") {
                row.querySelector(".role-column").innerText = user.role;
            }
        }

        /**
         * Refreshes all admin dashboard data from server
         */
        async function refreshData() {
            try {
                const data = await fetchAllUsers();

                if (data) {
                    data.forEach(user =>{
                        updatePermissionTable(user);
                        updatePendingTable(user);
                    })
                    filterTable(newAccountsTable, newAccountsSearch.value);
                    filterTable(permissionTable, permissionSearch.value);
                }

            } catch (error) {
                console.error('Error refreshing data:', error);
            }
        }

        pollingInterval = setInterval(refreshData, POLLING);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

    })
};

adminDashboard();