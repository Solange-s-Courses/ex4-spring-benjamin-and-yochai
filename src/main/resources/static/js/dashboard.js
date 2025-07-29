
const dashboardDom = function (){
    function formatDate(isoString) {
        const date = new Date(isoString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-indexed
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    function formatTime(isoString){
        const date = new Date(isoString);
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');

        return `${hours}:${minutes}`;
    }

    function getPositionStatusInfo(status) {
        const statusName = status?.name?.() || status; // Handle both enum and string

        const statusMap = {
            'ACTIVE': {
                cssClass: 'bg-success',
                text: 'פעילה'
            },
            'CANCELED': {
                cssClass: 'bg-danger',
                text: 'מבוטלת'
            },
            'FULFILLED': {
                cssClass: 'bg-primary',
                text: 'אוישה'
            },
            'FROZEN': {
                cssClass: 'bg-secondary',
                text: 'מוקפאת'
            }
        };

        return statusMap[statusName] || {
            cssClass: 'bg-light',
            text: statusName
        };
    }
    function getApplicationStatusInfo(status) {
        const statusName = status?.name?.() || status; // Handle both enum and string

        const statusMap = {
            'PENDING': {
                cssClass: 'bg-warning',
                text: 'ממתין'
            },
            'APPROVED': {
                cssClass: 'bg-success',
                text: 'אושר'
            },
            'REJECTED': {
                cssClass: 'bg-danger',
                text: 'נדחה'
            },
            'CANCELED': {
                cssClass: 'bg-secondary',
                text: 'בוטל'
            }
        };

        return statusMap[statusName] || {
            cssClass: 'bg-light',
            text: statusName
        };
    }

    function getInterviewStatusInfo(status) {
        const statusName = status?.name?.() || status; // Handle both enum and string

        const statusMap = {
            'SCHEDULED': {
                cssClass: 'bg-warning',
                text: 'ממתין לאישור'
            },
            'CONFIRMED': {
                cssClass: 'bg-success',
                text: 'מאושר'
            },
            'REJECTED': {
                cssClass: 'bg-danger',
                text: 'נדחה'
            },
            'COMPLETED': {
                cssClass: 'bg-primary',
                text: 'הושלם'
            },
            'CANCELED': {
                cssClass: 'bg-secondary',
                text:'מבוטל'
            }
        };

        return statusMap[statusName] || {
            cssClass: 'bg-light',
            text: statusName
        };
    }

    document.addEventListener("DOMContentLoaded",()=>{
        const totalApplicationsCount = document.getElementById("totalApplicationsCount");
        const pendingApplicationsCount = document.getElementById("pendingApplicationsCount");
        const upcomingInterviewCount = document.getElementById("upcomingInterviewCount");
        const myPositionsTbody = document.getElementById("myPositionsTbody");
        const myApplicationsTbody = document.getElementById("myApplicationsTbody");
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
            cols[1].textContent = data.position.location;
            cols[2].textContent = data.position.assignmentType;
            cols[3].innerHTML = `<span class="badge ${getPositionStatusInfo(data.position.status).cssClass}">${getPositionStatusInfo(data.position.status).text}</span>`
            cols[4].textContent = data.applicationCounter;
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
                    <span class="badge ${statusInfo.css}">
                        ${statusInfo.label}
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
                    <span class="badge" class="${statusInfo.cssClass}">
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
            /*const cols = row.querySelectorAll('td');
            cols[0].textContent = interview.application.position.jobTitle;

            const date = new Date(interview.interviewDate);
            cols[2].textContent = date.toLocaleDateString('en-GB');
            cols[3].textContent = interview.interviewDate;
            if(!interview.isVirtual){
                cols[4].textContent = interview.location;
            }
            if(username == interview.application.applicant.username){
                cols[5].textContent = 'line 147 dashboard.js';
            }
            else{
                cols[5].innerHTML = `<span class="badge ${getInterviewStatusInfo(interview.status).cssClass}">${getInterviewStatusInfo(interview.status).text}</span>`;
            }

            cols[6]
            cols[7]
            cols[8]*/
        }

        function addInterviewRow(interview, tbody){
            const row = document.createElement("tr");
            row.setAttribute("data-id", interview.id);

            const formattedDate = formatDate(interview.interviewDate)
            const formattedTime = formatTime(interview.interviewDate)
            const statusInfo = getInterviewStatusInfo(interview.status);

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
                <td>${interview.application.applicant.firstName} ${interview.application.applicant.lastName}</td>
                <td>${formattedDate}</td>
                <td>${formattedTime}</td>
                <td>${locationHtml}</td>
                <td>
                    <span class="badge ${statusInfo.cssClass}">${statusInfo.text}</span>
                    ${rejectionHtml}
                </td>
                <td>
                <span class="badge ${statusInfo.cssClass}">
                    ${statusInfo.text}
                </span>
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