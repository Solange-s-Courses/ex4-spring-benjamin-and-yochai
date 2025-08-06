import {showToast} from "./toastUtils.js";
import {genericSortingFunc, sortRowsByDate, sortRowsByText} from "./sortingFuncs.js";
import {formatDate, formatTime, getPositionStatusInfo, getInterviewStatusInfo, getApplicationStatusInfo} from "./textUtils.js"

const dashboardDom = function (){
    document.addEventListener("DOMContentLoaded",()=>{
        const totalApplicationsCount = document.getElementById("totalApplicationsCount");
        const pendingApplicationsCount = document.getElementById("pendingApplicationsCount");
        const upcomingInterviewCount = document.getElementById("upcomingInterviewCount");
        const myPositionsTbody = document.getElementById("myPositionsTbody");
        const myApplicationsTbody = document.getElementById("myApplicationsTbody");
        const interviewsTable = document.getElementById("interviews-table");
        const futureInterviewsTbody = document.getElementById("futureInterviewsTbody");
        const username = document.getElementById("username").value;


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

        function comparePositionRows(data, row){
            const cols = row.querySelectorAll('td');
            cols[0].querySelector("a").textContent = data.position.jobTitle;
            cols[1].textContent = data.position.location; //refactor to hebrew
            cols[2].textContent = data.position.assignmentType;
            cols[3].innerHTML = `<span class="badge ${getPositionStatusInfo(data.position.status).cssClass}">${getPositionStatusInfo(data.position.status).text}</span>`
            cols[4].textContent = data.activeApplications;
        }

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
                <td>${data.position.location}</td>
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

        function compareApplicationRows(application, row){
            const cols = row.querySelectorAll('td');
            cols[0].querySelector("a").textContent = application.position.jobTitle;
            cols[1].textContent = application.position.location;
            cols[2].textContent = application.position.assignmentType;
            cols[3].textContent = formatDate(application.applicationDate);
            cols[4].innerHTML = `<span class="badge ${getApplicationStatusInfo(application.status).cssClass}">${getApplicationStatusInfo(application.status).text}</span>`
        }

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

        function compareInterviewRows(interview, row){
            const cols = row.querySelectorAll('td');

            const commanderOffset = (cols.length === 8)? 1: 0;

            cols[0].textContent = interview.application.position.jobTitle;

            cols[1+commanderOffset].textContent = formatDate(interview.interviewDate)
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
                cols[4].innerHTML = `
                    <form method="post" class="mb-0 confirm-interview-form d-inline" data-interview-id="${interview.id}">
                        <!--input type="hidden" th:name="$ {_csrf.parameterName}" th:value="$ {_csrf.token}" /-->
                        <button type="submit" class="btn btn-success btn-sm">אשר</button>
                    </form>

                    <button type="button" class="btn btn-danger btn-sm reject-interview-btn" data-interview-id="${interview.id}">דחה</button>
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
                cols[4+commanderOffset].innerHTML = `<span class="badge ${infoStatus.cssClass}">${infoStatus.text}</span>`;
            }

            cols[5+commanderOffset].textContent = interview.notes;

        }

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

        // function addInterviewRow(data, tbody){
        //
        //     const newConfirmForms = newRow.querySelectorAll('.confirm-interview-form');
        //     newConfirmForms.forEach(form => {
        //         form.addEventListener('submit', async (event) => {
        //             await setConfirmInterviewListener(form, event);
        //         });
        //     });
        // }

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
                    //setTimeout(() => window.location.reload(), 1500);
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

        async function setConfirmInterviewListener(form, event) {
            event.preventDefault();
            const interviewId = form.dataset.interviewId;
            await confirmInterview(interviewId);
        }

        async function rejectInterview(interviewId) {
            try {
                const response = await fetch(`/restapi/interviews/${interviewId}/reject`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || ''
                    },
                    body: JSON.stringify({ reason: null })
                });

                const data = await response.json();

                if (response.ok) {
                    showToast(data.message);
                    //setTimeout(() => window.location.reload(), 1500);
                } else {
                    showToast(data.message, "danger");
                }
            } catch (error) {
                showToast("אירעה שגיאה בדחיית הראיון", "danger");
            }
            finally {
                await refreshData();
            }
        }

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

        const pollingInterval = setInterval(refreshData, 5000);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

    })
}

dashboardDom();