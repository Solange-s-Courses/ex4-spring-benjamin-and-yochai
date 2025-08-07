import {locationEnumToHebrew} from "./textUtils.js";

const positionsPageDom = ()=>{
    const params = new URLSearchParams();
    const POLLING = 5000;
    let pollingInterval = null;
    let currentFilters = {
        location: '',
        serviceType: ''
    };

    document.addEventListener('DOMContentLoaded', function () {
        const jobCardsDiv = document.getElementById("job-cards");
        const noPositionsMsg = document.getElementById("noPositionsMsg");
        const locationSelector = document.getElementById("location");
        const assigmentTypeSelector = document.getElementById("serviceType");
        const searchBox = document.getElementById("searchBox");
        const recentSearchesContainer = document.getElementById("recentSearchesList");
        const searchButton = document.getElementById("searchButton");
        const recentSearchButtons = document.querySelectorAll(".recentSearchBtn");

        /**
         * Filters job cards based on selected location and service type
         */
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

        /**
         * Updates select options with new data while preserving current selection
         * @param {HTMLSelectElement} selector - The select element to update
         * @param {string[]} options - Array of option values
         * @param {string} selectedValue - Currently selected value to preserve
         */
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
        }

        /**
         * Updates recent searches display with new search terms
         * @param {string[]} searches - Array of recent search terms
         */
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
                });
                recentSearchesContainer.appendChild(btn);
            })
        }

        // Recent search button handlers
        recentSearchButtons.forEach(button => {
            button.addEventListener("click", async () => {
                searchBox.value = button.textContent.trim();
                console.log(button.textContent.trim());
                await triggerSearch();
            });
        });

        /**
         * Fetches positions data from the server
         * @returns {Object|null} Positions data or null if failed
         */
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

        /**
         * Updates job cards display with new data
         * @param {Array} jobs - Array of job objects
         */
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

        /**
         * Refreshes all data from server and updates UI
         */
        async function refreshData() {
            try {
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
            } catch (error) {
                console.error('Error refreshing data:', error);
            }
        }

        /**
         * Triggers search with current search box value
         */
        async function triggerSearch() {
            params.set("search", searchBox.value.trim());
            await refreshData();
        }

        // Event listeners
        locationSelector.addEventListener("change", function() {
            filterJobs();
        });

        assigmentTypeSelector.addEventListener("change", function() {
            filterJobs();
        });

        pollingInterval = setInterval(refreshData, POLLING);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

        searchBox.addEventListener("keydown", async function(e) {
            if (e.key === "Enter") {
                await triggerSearch();
            }
        });

        searchButton.addEventListener("click", async function(e){
            await triggerSearch();
        })
    });
};

positionsPageDom();