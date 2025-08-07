import {locationEnumToHebrew} from "./textUtils.js";

const positionsPageDom = ()=>{
    const params = new URLSearchParams();
    let pollingInterval = null;


    document.addEventListener('DOMContentLoaded', function () {
        const jobCardsDiv = document.getElementById("job-cards");
        const noPositionsMsg = document.getElementById("noPositionsMsg");
        const locationSelector = document.getElementById("location");
        const assigmentTypeSelector = document.getElementById("serviceType");
        const searchBox = document.getElementById("searchBox");
        const recentSearchesContainer = document.getElementById("recentSearchesList");
        const searchButton = document.getElementById("searchButton");
        const recentSearchButtons = document.querySelectorAll(".recentSearchBtn");


        let currentFilters = {
            location: '',
            serviceType: ''
        };

        // async function loadRecentSearches() {
        //     try {
        //         const response = await fetch('/restapi/positions/recent-searches');
        //         if (!response.ok) return;
        //         const searches = await response.json();
        //         const container = document.getElementById("recentSearchesList");
        //         container.innerHTML = "";
        //         searches.forEach(search => {
        //             const btn = document.createElement("button");
        //             btn.type = "button";
        //             btn.className = "btn btn-sm btn-outline-secondary ms-2 mb-1";
        //             btn.textContent = search;
        //             btn.addEventListener('click', function() {
        //                 searchBox.value = search;
        //                 triggerSearch();
        //                 //loadRecentSearches();
        //             });
        //             container.appendChild(btn);
        //         });
        //     } catch (e) {}
        // }

        function filterJobs(){
            const selectedLocation = locationSelector.value;
            const selectedType = assigmentTypeSelector.value;
            let counter = 0;

            noPositionsMsg.classList.add("d-none");

            const cards = jobCardsDiv.querySelectorAll(".col");

            cards.forEach(card => {
                const locationText = card.dataset.location;
                const typeText = card.dataset.assignmentType;

                const matchesLocation = !selectedLocation || locationText === selectedLocation;
                const matchesType = !selectedType || typeText === selectedType;

                if (matchesLocation && matchesType) {
                    card.classList.remove("d-none");
                    counter ++;
                } else {
                    card.classList.add("d-none");
                }
            });
            if (counter === 0){
                noPositionsMsg.classList.remove("d-none");
            }
        }

        filterJobs();

        function updateSelectOptions(selector, options, selectedValue) {
            const firstOption = selector.firstElementChild;
            selector.innerHTML = '';
            selector.appendChild(firstOption);

            options.forEach(option => {
                const optionElement = document.createElement('option');
                optionElement.value = option;
                optionElement.textContent = option;
                optionElement.selected = (option === selectedValue);

                selector.appendChild(optionElement);
            });
            //if (selector === locationSelector) locationOptionsLoaded = true;
            //if (selector === assigmentTypeSelector) serviceTypeOptionsLoaded = true;
            //if (locationOptionsLoaded && serviceTypeOptionsLoaded) {
            //    triggerSearch();
            //}
        }

        function updateRecentSearches(searches) {
            recentSearchesContainer.innerHTML = "";
            searches.forEach(search => {
                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "btn btn-sm btn-outline-secondary ms-2 mb-1";
                btn.textContent = search;
                btn.addEventListener('click', async function () {
                    searchBox.value = search;
                    await triggerSearch();
                    //loadRecentSearches();
                });
                recentSearchesContainer.appendChild(btn);
            })
        }

        recentSearchButtons.forEach(button => {
            button.addEventListener("click", async () => {
                searchBox.value = button.textContent.trim();
                console.log(button.textContent.trim());
                await triggerSearch();
            });
        });

        async function fetchPositionsData() {
            try {
                const response = await fetch('/restapi/positions/active?' + params.toString(), {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                return await response.json();
            } catch (error) {
                console.error('Error fetching positions data:', error);
                return null;
            }
        }

        // async function fetchPositionsBySearch(query, location, serviceType) {
        //     const params = new URLSearchParams();
        //     params.append('search', query);
        //     //if (location) params.append('location', location);
        //     //if (serviceType) params.append('serviceType', serviceType);
        //     try {
        //         const response = await fetch(`/restapi/positions?${params.toString()}`);
        //         if (!response.ok) throw new Error("Network error");
        //         return await response.json();
        //     } catch (e) {
        //         console.error(e);
        //         return [];
        //     }
        // }

        // function renderPositions(positions) {
        //     jobCardsDiv.innerHTML = "";
        //     if (positions.length === 0) {
        //         noPositionsMsg.classList.remove("d-none");
        //         return;
        //     }
        //     noPositionsMsg.classList.add("d-none");
        //     positions.forEach(pos => {
        //         const jobCard = document.createElement('div');
        //         jobCard.className = 'col';
        //         jobCard.innerHTML = `
        //             <div class="card h-100 shadow-sm rounded-4">
        //                 <div class="card-body d-flex flex-column">
        //                     <h5 class="card-title fw-bold">${pos.jobTitle || 'שם משרה'}</h5>
        //                     <p class="mb-1"><strong>מיקום:</strong> <span>${pos.location || ''}</span></p>
        //                     <p class="mb-1"><strong>סוג שירות:</strong> <span>${pos.assignmentType || ''}</span></p>
        //                     <p class="mb-1"><strong>דרישות:</strong> <span>${pos.requirements ? (pos.requirements.length > 50 ? pos.requirements.substring(0, 50) + '...' : pos.requirements) : ''}</span></p>
        //                     <p class="card-text text-muted flex-grow-1">${pos.description ? (pos.description.length > 100 ? pos.description.substring(0, 100) + '...' : pos.description) : ''}</p>
        //                     <p class="mb-1"><strong>מפורסם ע"י:</strong> <span>${pos.publisherName || ''}</span></p>
        //                     <a href="/positions/${pos.id}" class="btn btn-outline-primary mt-3 rounded-3">לפרטי המשרה</a>
        //                 </div>
        //             </div>
        //         `;
        //         jobCardsDiv.appendChild(jobCard);
        //     });
        // }

        function updateJobCards(jobs) {
            jobCardsDiv.innerHTML = '';

            jobs.forEach(job => {
                const jobCard = document.createElement('div');
                jobCard.className = 'col';
                jobCard.setAttribute('data-location', job.location);
                jobCard.setAttribute('data-assignment-type', job.assignmentType);

                jobCard.innerHTML = `
                    <div class="card h-100 shadow-sm rounded-4">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title fw-bold">${job.jobTitle || 'שם משרה'}</h5>
                            <p class="mb-1"><strong>מיקום:</strong> <span>${locationEnumToHebrew(job.location) || job.location}</span></p>
                            <p class="mb-1"><strong>סוג שירות:</strong> <span>${job.assignmentType || ''}</span></p>
                            <p class="mb-1">
                                <strong>דרישות:</strong>
                                <span>${job.requirements ? (job.requirements.length > 50 ? job.requirements.substring(0, 50) + '...' : job.requirements) : ''}</span>
                            </p>
                            <p class="card-text text-muted flex-grow-1">
                                ${job.description ? (job.description.length > 100 ? job.description.substring(0, 100) + '...' : job.description) : ''}
                            </p>
                            <p class="mb-1"><strong>מפורסם ע"י:</strong> <span>${job.publisher.username || ''}</span></p>
                            <a href="/positions/${job.id}" class="btn btn-outline-primary mt-3 rounded-3">לפרטי המשרה</a>
                        </div>
                    </div>
                `;

                jobCardsDiv.appendChild(jobCard);
            });
        }

        async function refreshData() {
            try {
                // if (window.startPolling) {
                //     window.startPolling();
                // }
                currentFilters.location = locationSelector.value;
                currentFilters.serviceType = assigmentTypeSelector.value;

                const data = await fetchPositionsData();

                if (data) {
                    if (data.jobs) {
                        updateJobCards(data.jobs);
                    }
                    if (data.locations) {
                        updateSelectOptions(locationSelector, data.locations, currentFilters.location);
                    }
                    if (data.serviceTypes) {
                        updateSelectOptions(assigmentTypeSelector, data.serviceTypes, currentFilters.serviceType);
                    }
                    if (data.recentSearches){
                        updateRecentSearches(data.recentSearches)
                    }

                    filterJobs();
                }
                
                // if (window.onPollingComplete) {
                //     window.onPollingComplete();
                // }
            } catch (error) {
                // if (window.onPollingComplete) {
                //     window.onPollingComplete();
                // }
                console.error('Error refreshing data:', error);
            }
        }

        async function triggerSearch() {

            params.set("search", searchBox.value.trim());
            //const query = searchBox.value.trim();
            //const location = locationSelector.value;
            //const serviceType = assigmentTypeSelector.value;
            //fetchPositionsBySearch(query, location, serviceType).then(renderPositions);
            await refreshData();
        }

        locationSelector.addEventListener("change", function() {
            filterJobs();
            //triggerSearch();
            //loadRecentSearches();
        });

        assigmentTypeSelector.addEventListener("change", function() {
            filterJobs();
            //triggerSearch();
            //loadRecentSearches();
        });

        pollingInterval = setInterval(refreshData, 10000);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

        searchBox.addEventListener("keydown", async function(e) {
            if (e.key === "Enter") {
                await triggerSearch();
                //loadRecentSearches();
            }
        });

        searchButton.addEventListener("click", async function(e){
            await triggerSearch();
        })

        // searchBox.addEventListener("input", function(e) {
        //     if (searchBox.value.trim() === "") {
        //         //triggerSearch();
        //         //loadRecentSearches();
        //     }
        // });

        //triggerSearch();
        //loadRecentSearches();
    });
};

positionsPageDom();