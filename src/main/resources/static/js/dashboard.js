
const dashboardDom = function (){
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
                const itemId = item.id.toString();
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

        }

        function compareApplicationRows(application, row){
            const cols = row.querySelectorAll('td');
            cols[0].querySelector("a").textContent = application.position.jobTitle;
            cols[1].textContent = application.position.location;
            cols[2].textContent = application.position.assignmentType;
            const date = new Date(application.applicationDate);
            cols[3].textContent = date.toLocaleDateString('en-GB');
            cols[4].innerHTML = `<span class="badge ${getApplicationStatusInfo(application.status).cssClass}">${getApplicationStatusInfo(application.status).text}</span>`
        }

        function addApplicationRow(data, tbody){

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

        function addInterviewRow(data, tbody){

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