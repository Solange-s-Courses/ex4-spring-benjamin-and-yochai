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

        // דרישות מינימום דינמיות עם מחיקה
        function updateRemoveButtons() {
            const rows = document.querySelectorAll('#requirementsContainer .input-group');
            rows.forEach(row => {
                const btn = row.querySelector('.remove-requirement');
                btn.style.display = (rows.length > 1) ? 'inline-block' : 'none';
            });
        }

        document.getElementById('addRequirementBtn').onclick = function() {
            const container = document.getElementById('requirementsContainer');
            const row = document.createElement('div');
            row.className = 'input-group mb-2';
            row.innerHTML = `
                <input type="text" name="requirements" placeholder="הכנס  דרישת מינימום לתפקיד (לדוגמא - רובאי 03)" class="form-control rounded-3" required>
                <button type="button" class="btn btn-outline-danger remove-requirement">
                    <i class="bi bi-trash"></i>
                </button>
            `;
            container.appendChild(row);
            updateRemoveButtons();
        };

        document.getElementById('requirementsContainer').addEventListener('click', function(e) {
            if (e.target.closest('.remove-requirement')) {
                const rows = document.querySelectorAll('#requirementsContainer .input-group');
                if (rows.length > 1) {
                    e.target.closest('.input-group').remove();
                    updateRemoveButtons();
                }
            }
        });

        updateRemoveButtons();
    });

}

positionDom();
