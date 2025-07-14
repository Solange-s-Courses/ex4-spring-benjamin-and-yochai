
const positionsPageDom = ()=>{
    document.addEventListener('DOMContentLoaded', function () {
        const jobCardsDiv = document.getElementById("job-cards");
        const locationSelector = document.getElementById("location");
        const assigmentTypeSelector = document.getElementById("serviceType");
        const urlParams = new URLSearchParams(window.location.search);

        if (urlParams.has('success')) {
            const successToast = new bootstrap.Toast(document.getElementById('successToast'), {
                autohide: true,
                delay: 5000 // 5 seconds
            });

            successToast.show();

            // Clean up URL (optional)
            const newUrl = window.location.pathname;
            window.history.replaceState({}, document.title, newUrl);
        }

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

        locationSelector.addEventListener("change", filterJobs);
        assigmentTypeSelector.addEventListener("change", filterJobs)
    });

};

positionsPageDom();

// here we need to add filtration, and sorting