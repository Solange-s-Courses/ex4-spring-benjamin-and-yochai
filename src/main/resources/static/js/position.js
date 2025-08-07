/**
 * Position details page functionality module
 * @module position
 */

import {showToast} from "./toastUtils.js";
import {getApplicationStatusInfo, formatDateTime} from "./textUtils.js";

/**
 * Initializes position page functionality including application management and status updates
 */
function positionDom() {
    const POLLING = 5000;

    document.addEventListener("DOMContentLoaded", () => {
        //users
        const applicationPendingStatus = document.getElementById("applicationPendingStatus");
        const noApplicationStatus = document.getElementById("noApplicationStatus");
        const cancelApplicationBtn = document.getElementById("cancel-application-btn");
        const applyPositionBtn = document.getElementById("apply-position-btn");
        //commander
        const changeStatusForm = document.getElementById("changeStatusForm");
        const applicantTable = document.getElementById("applicantTable");
        let pollingInterval = null;

        /**
         * Sends application request (apply/cancel) to the server
         * @param {HTMLButtonElement} btn - The button that triggered the request
         * @param {HTMLElement} divToHide - Element to hide on success
         * @param {HTMLElement} divToShow - Element to show on success
         */
        const sendReq = async (btn, divToHide, divToShow) => {
            try {
                btn.disabled = true;
                const response = await fetch(`/restapi/applications/${btn.dataset.positionId}/${btn.dataset.action}`,
                    {
                        method: "POST",
                        headers: getCSRFHeaders()
                    });

                let data;

                try {
                    data = await response.json();
                } catch (jsonError) {
                    data = {};
                }

                if (!response.ok) {
                    const serverMessage = data.message || `שגיאה בשרת (status: ${response.status})`;
                    throw new Error(serverMessage);
                }

                const msg = data.message;

                divToHide.classList.add("d-none");
                divToShow.classList.remove("d-none");

                showToast(msg);

            } catch (e) {
                const msg = e.message || 'שגיאה בשליחת הבקשה';
                showToast(msg, "danger");
            } finally {
                btn.disabled = false;
            }
        }

        // Apply/Cancel application button handlers
        if (cancelApplicationBtn) {
            cancelApplicationBtn.addEventListener("click", async () => {
                await sendReq(cancelApplicationBtn, applicationPendingStatus, noApplicationStatus);
            })
        }

        if (applyPositionBtn) {
            applyPositionBtn.addEventListener("click", async () => {
                await sendReq(applyPositionBtn, noApplicationStatus, applicationPendingStatus);
            })
        }

        /**
         * Gets CSRF headers for API requests
         * @returns {Object} Headers object with CSRF token
         */
        const getCSRFHeaders = () => {
            const csrfMeta = document.querySelector('meta[name="_csrf"]');
            const headers = {'Content-Type': 'application/json'};

            if (csrfMeta) {
                const csrfToken = csrfMeta.getAttribute('content');
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
                headers[csrfHeader] = csrfToken;
            }

            return headers;
        };

        // Position status change functionality (for commanders)
        if (changeStatusForm) {
            const selector = changeStatusForm.querySelector("select");
            const positionId = selector.dataset.positionId;

            changeStatusForm.addEventListener("submit", async (e) => {
                e.preventDefault();
                const originalStatus = selector.dataset.status;
                const choosenStatus = selector.value;

                if (originalStatus === choosenStatus) return;

                try {
                    const response = await fetch(`/restapi/positions/${positionId}/status`, {
                        method: "PUT",
                        headers: getCSRFHeaders(),
                        body: JSON.stringify({status: choosenStatus})
                    })
                    if (!response.ok) {
                        const errorData = await response.json().catch(() => ({message: `HTTP error! status: ${response.status}`}));
                        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
                    }

                    const data = await response.json();
                    showToast(data.message);
                    selector.dataset.status = choosenStatus;
                } catch (e) {
                    showToast(e.message || "שגיאה בעדכון סטטוס המשרה", "danger")
                } finally {
                    await reload();
                }
            })

            /**
             * Reloads applicant table data from server
             */
            async function reload() {
                try {
                    const response = await fetch(`/restapi/positions/${positionId}/poll`, {
                        method: "GET"
                    })
                    if (!response.ok) throw Error("Failed to poll");

                    const data = (await response.json()).applications;
                    const tbody = applicantTable.querySelector("tbody");

                    const emptyRow = tbody.querySelector("tr td[colspan='4']");
                    if (emptyRow && data.length > 0) {
                        emptyRow.parentElement.remove();
                    }

                    for (const app of data) {
                        const existingRow = tbody.querySelector(`tr[application-id="${app.id}"]`);
                        const statusInfo = getApplicationStatusInfo(app.status);

                        if (existingRow) {
                            const cols = existingRow.querySelectorAll('td');
                            cols[3].innerHTML = `
                            <span class="badge ${statusInfo.cssClass}"> ${statusInfo.text}</span>
                        `;
                        } else {
                            const tr = document.createElement("tr");
                            tr.setAttribute("application-id", app.id);

                            tr.innerHTML = `
                            <td>
                                <a href="${'/application/' + app.id}">
                                    ${app.applicant.firstName + ' ' + app.applicant.lastName}
                                </a>
                            </td>
                            <td>${app.applicant.email}</td>
                            <td>${formatDateTime(app.applicationDate)}</td>
                            <td>
                                <span class="badge ${statusInfo.cssClass}"> ${statusInfo.text} </span>
                            </td>
                        `;

                            tbody.appendChild(tr);
                        }
                    }
                } catch (e) {
                    console.error("error polling: " + e);
                }
            }

            pollingInterval = setInterval(reload, POLLING);
        }

        if (pollingInterval) {
            window.addEventListener("beforeunload", () => {
                clearInterval(pollingInterval);
            })
        }

        /**
         * Initializes share button functionality
         * Uses Web Share API if available, falls back to clipboard copy
         */
        const initShareButton = () => {
            const shareBtn = document.getElementById("shareBtn");
            if (!shareBtn) return;

            shareBtn.addEventListener("click", () => {
                if (navigator.share) {
                    navigator.share({
                        title: document.querySelector('h2')?.textContent || '',
                        text: 'בדוק את המשרה הזו:',
                        url: window.location.href
                    }).catch(() => {
                        navigator.clipboard.writeText(window.location.href).then(() => {
                            const messageBox = document.getElementById('copy-message');
                            if (messageBox) {
                                messageBox.classList.remove("d-none");
                                setTimeout(() => messageBox.classList.add('d-none'), 3000);
                            }
                        });
                    });
                } else {
                    navigator.clipboard.writeText(window.location.href).then(() => {
                        showToast("הקישור הועתק בהצלחה!");
                    });
                }
            });
        };

        initShareButton();
    });
}

positionDom();