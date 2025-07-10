function positionDom(){
    document.addEventListener("DOMContentLoaded", function () {
        const jobTitleSelect = document.getElementById("jobTitle");
        const otherJobTitleInput = document.getElementById("otherJobTitleInput");

        function toggleOtherInput() {
            if (jobTitleSelect.value === "אחר") {
                otherJobTitleInput.style.display = "block";
                otherJobTitleInput.value = "";
            } else {
                otherJobTitleInput.value = "";
                otherJobTitleInput.style.display = "none";
            }
        }

        toggleOtherInput();
        jobTitleSelect.addEventListener("change", toggleOtherInput);
    });

}

positionDom();
