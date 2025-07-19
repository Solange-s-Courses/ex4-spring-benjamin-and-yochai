
const positionsPageDom = ()=>{
    document.addEventListener('DOMContentLoaded', function () {
        const jobCardsDiv = document.getElementById("job-cards");
        const locationSelector = document.getElementById("location");
        const assigmentTypeSelector = document.getElementById("serviceType");

        let currentFilters = {
            location: '',
            serviceType: ''
        };

        function filterJobs(){
            const selectedLocation = locationSelector.value;
            const selectedType = assigmentTypeSelector.value;

            const cards = jobCardsDiv.querySelectorAll(".col");

            cards.forEach(card => {
                const locationText = card.dataset.location;
                const typeText = card.dataset.assignmentType;

                const matchesLocation = !selectedLocation || locationText === selectedLocation;
                const matchesType = !selectedType || typeText === selectedType;

                if (matchesLocation && matchesType) {
                    card.classList.remove("d-none");
                } else {
                    card.classList.add("d-none");
                }
            });
        }

        function updateSelectOptions(selector, options, selectedValue) {
            const firstOption = selector.firstElementChild;
            selector.innerHTML = '';
            selector.appendChild(firstOption);

            // Add new options
            options.forEach(option => {
                const optionElement = document.createElement('option');
                optionElement.value = option;
                optionElement.textContent = option;
                optionElement.selected = (option === selectedValue);

                selector.appendChild(optionElement);
            });
        }

        async function fetchPositionsData() {
            try {
                // You may need to adjust this endpoint based on your backend API
                const response = await fetch('/restapi/positions/active', {
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

        function updateJobCards(jobs) {
            if (!jobs || jobs.length === 0) {
                jobCardsDiv.innerHTML = `
                    <div class="col-12">
                        <div class="alert alert-warning text-center rounded-3 shadow-sm">
                            אין משרות זמינות להצגה.
                        </div>
                    </div>
                `;
                return;
            }

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
                            <p class="mb-1"><strong>מיקום:</strong> <span>${job.location || ''}</span></p>
                            <p class="mb-1"><strong>סוג שירות:</strong> <span>${job.assignmentType || ''}</span></p>
                            <p class="mb-1">
                                <strong>דרישות:</strong>
                                <span>${job.requirements ? (job.requirements.length > 50 ? job.requirements.substring(0, 50) + '...' : job.requirements) : ''}</span>
                            </p>
                            <p class="card-text text-muted flex-grow-1">
                                ${job.description ? (job.description.length > 100 ? job.description.substring(0, 100) + '...' : job.description) : ''}
                            </p>
                            <a href="/positions/${job.id}" class="btn btn-outline-primary mt-3 rounded-3">לפרטי המשרה</a>
                        </div>
                    </div>
                `;

                jobCardsDiv.appendChild(jobCard);
            });
        }

        async function refreshData() {
            try {
                currentFilters.location = locationSelector.value;
                currentFilters.serviceType = assigmentTypeSelector.value;

                const data = await fetchPositionsData();

                if (data) {
                    // Update job cards
                    if (data.jobs) {
                        updateJobCards(data.jobs);
                    }

                    if (data.locations) {
                        updateSelectOptions(locationSelector, data.locations, currentFilters.location);
                    }

                    if (data.serviceTypes) {
                        updateSelectOptions(assigmentTypeSelector, data.serviceTypes, currentFilters.serviceType);
                    }

                    filterJobs();
                }
            } catch (error) {
                console.error('Error refreshing data:', error);
            }
        }

        locationSelector.addEventListener("change", filterJobs);
        assigmentTypeSelector.addEventListener("change", filterJobs)
        const pollingInterval = setInterval(refreshData, 10000);

        window.addEventListener('beforeunload', function() {
            clearInterval(pollingInterval);
        });

    });

};

positionsPageDom();

// here we need to add filtration, and sorting