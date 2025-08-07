/**
 * Dashboard functionality module for user dashboard
 * @module dashboard
 */

import {showToast} from "./toastUtils.js";
import {genericSortingFunc, sortRowsByDate, sortRowsByText} from "./sortingFuncs.js";
import {formatDate, formatTime, getPositionStatusInfo, getInterviewStatusInfo, getApplicationStatusInfo, locationEnumToHebrew} from "./textUtils.js"

/**
 * Initializes dashboard functionality including data polling and table management
 */
const dashboardDom = function (){
    const POLLING = 5000;

    document.addEventListener("DOMContentLoaded",()=>{
        const totalApplicationsCount = document.getElementById("totalApplicationsCount");
        const pendingApplicationsCount = document.getElementById("pendingApplicationsCount");
        const upcomingInterviewCount = document.getElementById("upcomingInterviewCount");
        const myPositionsTbody = document.getElementById("myPositionsTbody");
        const myApplicationsTbody = document.getElementById("myApplicationsTbody");
        const interviewsTable = document.getElementById("interviews-table");
        const futureInterviewsTbody = document.getElementById("futureInterviewsTbody");
        const username = document.getElementById("username").value;
        const modal = document.getElementById('rejectionModal');
        const modalInstance = new bootstrap.Modal(modal);
        const rejectionReasonTextArea = document.getElementById('rejectionReason');

        /**
         * Reloads table data by comparing existing rows with new data
         * @param {Array} data - New data array
         * @param {HTMLTableSectionElement} tbody - Table body element
         * @param {Function} compareRows - Function to compare and update existing rows
         * @param {Function} addRow - Function to add new rows
         */
        function reloadTable(data, tbody, compareRows, addRow){
            if(!tbody) return;

            const existingRows = tbody.querySelectorAll("tr[data-id]");
            const existingRowsMap = new Map();

            existingRows.forEach(row => {
                const id = row.getAttribute('data-id');
                existingRowsMap.set(id, row);
            });

            data.forEach(item => {
                const itemId = (item?.id ?? item.position.id).toString();

                const existingRow = existingRowsMap.get(itemId);

                if (existingRow) {
                    compareRows(item, existingRow);
                } else {
                    addRow(item, tbody);
                }
            });
        }

        /**
         * Compares and updates position row data
         * @param {Object} data - Position data object
         * @param {HTMLTableRowElement} row - Table row element to update
         */
        function comparePositionRows(data, row){
            const cols = row.querySelectorAll('td');
            cols[0].querySelector("a").textContent = data.position.jobTitle;
            cols[1].textContent = locationEnumToHebrew(data.position.location);
            cols[2].textContent = data.position.assignmentType;
            cols[3].innerHTML = `<span class="badge ${getPositionStatusInfo(data.position.status).cssClass}">${getPositionStatusInfo(data.position.status).text}</span>`
            cols[4].textContent = data.activeApplications;
        }

        /**
         * Adds new position row to table
         * @param {Object} data - Position data object
         * @param {HTMLTableSectionElement} tbody - Table body element
         */
        function addPositionRow(data, tbody){
            const row = document.createElement("tr");
            row.setAttribute("data-id", data.position.id);

            const statusInfo = getPositionStatusInfo(data.position.status);

            row.innerHTML = `
                <td>
                    <a href="/positions/${data.position.id}" class="text-decoration-none">
                        ${data.position.jobTitle}
                    </a>
                </td>
                <td>${locationEnumToHebrew(data.position.location)}</td>
                <td>${data.position.assignmentType}</td>
                <td>
                    <span class="badge ${statusInfo.cssClass}">
                        ${statusInfo.text}
                    </span>
                </td>
                <td>${data.activeApplications}</td>
                <td>
                    <div class="d-flex flex-column gap-2 align-items-center">
                        <div class="btn-group" role="group">
                            <a href="/positions/${data.position.id}" class="btn btn-outline-primary btn-sm rounded-3" title="צפה">
                                <i class="bi bi-eye"></i>
                                נהל משרה
                            </a>
                        </div>
                    </div>
                </td>
            `;

            tbody.appendChild(row);
        }

        /**
         * Compares and updates application row data
         * @param {Object} application - Application data object
         * @param {HTMLTableRowElement} row - Table row element to update
         */
        function compareApplicationRows(application, row){
            const cols = row.querySelectorAll('td');
            cols[0].querySelector("a").textContent = application.position.jobTitle;
            cols[1].textContent = locationEnumToHebrew(application.position.location);
            cols[2].textContent = application.position.assignmentType;
            cols[3].textContent = formatDate(application.applicationDate);
            cols[4].innerHTML = `<span class="badge ${getApplicationStatusInfo(application.status).cssClass}">${getApplicationStatusInfo(application.status).text}</span>`
        }

        /**
         * Adds new application row to table
         * @param {Object} application - Application data object
         * @param {HTMLTableSectionElement} tbody - Table body element
         */
        function addApplicationRow(application, tbody){
            const row = document.createElement("tr");
            row.setAttribute("data-id", application.id);

            const statusInfo = getApplicationStatusInfo(application.status);

            row.innerHTML = `
                <td>
                    <a href='/positions/${application.position.id}'
                        class="text-decoration-none">
                            ${application.position.jobTitle}
                    </a>
                </td>
                <td>${application.position.location}</td>
                <td>${application.position.assignmentType}</td>
                <td>${formatDate(application.applicationDate)}</td>
                <td>
                    <span class="badge ${statusInfo.cssClass}">
                        ${statusInfo.text}
                    </span>
                </td>
                <td>
                    <div class="btn-group" role="group">
                        <a href="/positions/${application.position.id}"
                            class="btn btn-outline-primary btn-sm rounded-3">
                            <i class="bi bi-eye me-1"></i>
                            צפה
                        </a>
                    </div>
                </td>
            `;

            tbody.appendChild(row);
        }

        /**
         * Compares and updates interview row data
         * @param {Object} interview - Interview data object
         * @param {HTMLTableRowElement} row - Table row element to update
         */
        function compareInterviewRows(interview, row){
            const cols = row.querySelectorAll('td');

            const commanderOffset = (cols.length === 8)? 1: 0;

            cols[0].textContent = interview.application.position.jobTitle;

            cols[1+commanderOffset].textContent = formatDate(interview.interviewDate);
            cols[2+commanderOffset].textContent = formatTime(interview.interviewDate);

            if(interview.isVirtual) {
                cols[3 + commanderOffset].innerHTML =
                    `<a href="${interview.jitsiLink}" target="_blank" class="btn btn-sm btn-primary">
                        <i class="bi bi-camera-video"></i> הצטרף לפגישה
                    </a>`;
            } else {
                cols[3 + commanderOffset].innerHTML = interview.location;
            }

            if (username === interview.application.applicant.username && interview.status === "SCHEDULED") {
                cols[4+commanderOffset].innerHTML = `
                    <div class="d-flex gap-2 justify-content-center align-items-center">
                    
                    <form method="post" class="mb-0 confirm-interview-form d-inline" data-interview-id="${interview.id}">
                        <button type="submit" class="btn btn-success btn-sm">אשר</button>
                    </form>

                        <button type="button" class="btn btn-danger btn-sm reject-interview-btn" data-interview-id="${interview.id}">דחה</button>
                    </div>
                `;

                const form = row.querySelector(".confirm-interview-form");
                form.addEventListener("submit", async (event) => {
                    await setConfirmInterviewListener(form, event);
                });

                row.querySelector(".reject-interview-btn").addEventListener("click", async (event) => {
                    event.preventDefault();
                    await rejectInterview(interview.id);
                });

            }
            else{
                const infoStatus = getInterviewStatusInfo(interview.status);
                let reason = `<span class="badge ${infoStatus.cssClass}">${infoStatus.text}</span>`;

                if (interview.status === 'REJECTED' && interview.rejectionReason &&
                    interview.application.applicant.username !== username) {
                    reason += `<div class="mt-1"><small class="text-muted"><strong>סיבה:</strong> ${interview.rejectionReason}</small></div>`;
                }
                cols[4+commanderOffset].innerHTML = reason;
            }

            cols[5+commanderOffset].textContent = interview.notes;

        }

        /**
         * Adds new interview row to table
         * @param {Object} interview - Interview data object
         * @param {HTMLTableSectionElement} tbody - Table body element
         */
        function addInterviewRow(interview, tbody){
            const row = document.createElement("tr");
            row.setAttribute("data-id", interview.id);

            const formattedDate = formatDate(interview.interviewDate)
            const formattedTime = formatTime(interview.interviewDate)
            const statusInfo = getInterviewStatusInfo(interview.status);

            let commanderHtml;
            if(interview.application.position.publisher.username === username){
                commanderHtml = `<td>${interview.application.applicant.firstName} ${interview.application.applicant.lastName}</td>`;
            }
            else{
                commanderHtml = ``;
            }

            let locationHtml = '';
            if (interview.isVirtual) {
                locationHtml = `
                    <div class="text-center">
                        <a href="${interview.jitsiLink}" target="_blank" class="btn btn-sm btn-primary">
                            <i class="bi bi-camera-video"></i> הצטרף לפגישה
                        </a>
                    </div>
                `;
            } else {
                locationHtml = `<span>${interview.location}</span>`;
            }

            const rejectionHtml = (interview.status === 'REJECTED' && interview.rejectionReason)
                ? `<div class="mt-1"><small class="text-muted"><strong>סיבה:</strong> ${interview.rejectionReason}</small></div>`
                : '';

            row.innerHTML = `
                <td>${interview.application.position.jobTitle}</td>
                ${commanderHtml}
                <td>${formattedDate}</td>
                <td>${formattedTime}</td>
                <td>${locationHtml}</td>
                <td>
                    <span class="badge ${statusInfo.cssClass}">${statusInfo.text}</span>
                    ${rejectionHtml}
                </td>
                <td>${interview.notes || ''}</td>
                <td>
                    <span class="badge ${interview.application.applicant.username === username ? 'bg-primary' : 'bg-success'}">
                        ${interview.application.applicant.username === username ? 'מרואיין' : 'מראיין'}
                    </span>
                </td>
            `;

            tbody.appendChild(row);
        }

        /**
         * Confirms an interview by sending request to server
         * @param {string} interviewId - Interview ID to confirm
         */
        async function confirmInterview(interviewId) {
            try {
                const response = await fetch(`/restapi/interviews/${interviewId}/confirm`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
                    }
                });

                const data = await response.json();

                if (response.ok) {
                    showToast(data.message);
                } else {
                    showToast(data.message, "danger");
                }
            }
            catch (error) {
                showToast("אירעה שגיאה באישור הראיון", "danger");
            }
            finally {
                await refreshData();
            }
        }

        /**
         * Sets up confirm interview form listener
         * @param {HTMLFormElement} form - Form element
         * @param {Event} event - Form submit event
         */
        async function setConfirmInterviewListener(form, event) {
            event.preventDefault();
            const interviewId = form.dataset.interviewId;
            await confirmInterview(interviewId);
        }

        /**
         * Rejects an interview with optional reason
         * @param {string} interviewId - Interview ID to reject
         */
        async function rejectInterview(interviewId) {
            try {
                rejectionReasonTextArea.value = '';

                const confirmBtn = document.getElementById('confirmRejectBtn');
                const newConfirmBtn = confirmBtn.cloneNode(true);
                confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);

                newConfirmBtn.addEventListener('click', async () => {
                    const reason = document.getElementById('rejectionReason').value.trim();

                    try {
                        const response = await fetch(`/restapi/interviews/${interviewId}/reject`, {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
                            },
                            body: JSON.stringify({reason: reason || null})
                        });

                        const data = await response.json();

                        if (!response.ok) throw Error(data.message);

                        showToast(data.message);
                        modalInstance.hide();
                    } catch (e) {
                        showToast(e.message, "danger");
                    }
                    finally {
                        await refreshData();
                    }
                });

            } catch (error) {
                console.error(error);
            }
            finally {
                modalInstance.show();
            }
        }

        // Set up event listeners for existing forms
        const confirmForms = document.querySelectorAll('.confirm-interview-form');
        confirmForms.forEach(form => {
            form.addEventListener('submit', async (event) => {
                await setConfirmInterviewListener(form, event);
            });
        });

        const rejectButtons = document.querySelectorAll('.reject-interview-btn');
        rejectButtons.forEach(button => {
            button.addEventListener('click', async (event) => {
                event.preventDefault();
                const interviewId = button.dataset.interviewId;
                await rejectInterview(interviewId);
            });
        });

        /**
         * Refreshes all dashboard data from server
         */
        async function refreshData(){
            try{
                const response = await fetch("restapi/dashboard/poll", {
                    method: "GET"
                })
                if (!response.ok) throw Error();

                const data = await response.json();

                if(data.stats){
                    totalApplicationsCount.textContent = data.stats.totalApplicationsCount;
                    pendingApplicationsCount.textContent = data.stats.pendingApplicationsCount;
                    upcomingInterviewCount.textContent = data.stats.upcomingInterviewCount;
                }
                if(data.myPositions){
                    reloadTable(data.myPositions, myPositionsTbody, comparePositionRows, addPositionRow);
                }
                if(data.myApplication){
                    reloadTable(data.myApplication, myApplicationsTbody, compareApplicationRows, addApplicationRow);
                }
                if(data.interviews){
                    reloadTable(data.interviews, futureInterviewsTbody, compareInterviewRows, addInterviewRow);

                    const sortType = interviewsTable.dataset.sortType;
                    if (sortType){
                        const button = interviewsTable.querySelector(`[data-sort="${sortType}"]`);
                        const colIndex = parseInt(button.dataset.col);

                        if (sortType === 'date') {
                            genericSortingFunc(interviewsTable, colIndex, sortRowsByDate, false);
                        } else if (sortType === 'role') {
                            genericSortingFunc(interviewsTable, colIndex, sortRowsByText, false);
                        }
                    }

                }
            }catch (e){
                console.error("error polling: " + e);
            }
        }

        const pollingInterval = setInterval(refreshData, POLLING);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

    })
}

dashboardDom();