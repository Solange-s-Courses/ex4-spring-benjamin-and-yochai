function positionDom(){
    document.addEventListener("DOMContentLoaded", function () {
        const jobTitleSelect = document.getElementById("jobTitle");
        const otherJobTitleInput = document.getElementById("otherJobTitleInput");
        const locationSelect = document.getElementById("location");
        const descriptionTextarea = document.getElementById("description");
        const form = document.querySelector('form');
        const submitButton = document.querySelector('button[type="submit"]');

        function clearErrors() {
            document.querySelectorAll(".client-error").forEach(div => {
                div.textContent = "";
            });
        }

        function showError(message, field) {
            const errorDiv = document.querySelector(`[data-error="${field}"]`);
            if (errorDiv) {
                errorDiv.textContent = message;
            }
        }

        function validateJobTitle() {
            const selectedValue = jobTitleSelect.value;
            
            if (!selectedValue || selectedValue === "") {
                showError('חובה לבחור תפקיד', 'jobTitle');
                return false;
            }
            
            if (selectedValue === "אחר") {
                const otherValue = otherJobTitleInput.value.trim();
                if (!otherValue) {
                    showError('חובה להזין תפקיד', 'otherJobTitleInput');
                    return false;
                }
                if (otherValue.length < 2) {
                    showError('תפקיד אחר חייב להכיל לפחות 2 תווים', 'otherJobTitleInput');
                    return false;
                } else {
                    showError('', 'otherJobTitleInput');
                }
            } else {
                showError('', 'otherJobTitleInput');
            }
            
            return true;
        }

        function validateLocation() {
            const selectedValue = locationSelect.value;
            
            if (!selectedValue || selectedValue === "") {
                showError('חובה לבחור מיקום', 'location');
                return false;
            }
            
            return true;
        }

        function validateAssignmentType() {
            const radioButtons = document.querySelectorAll('input[name="assignmentType"]');
            const selected = Array.from(radioButtons).some(radio => radio.checked);
            
            if (!selected) {
                showError('חובה לבחור סוג שיבוץ', 'assignmentType');
                return false;
            }
            
            return true;
        }

        function validateDescription() {
            const value = descriptionTextarea.value.trim();
            
            if (!value) {
                showError('חובה להזין תיאור תפקיד', 'description');
                return false;
            }
            
            if (value.length < 10) {
                showError('תיאור התפקיד חייב להכיל לפחות 10 תווים', 'description');
                return false;
            }
            
            if (value.length > 500) {
                showError('תיאור התפקיד לא יכול לעלות על 500 תווים', 'description');
                return false;
            }
            
            showError('', 'description');
            return true;
        }

        function validateRequirements() {
            const requirementInputs = document.querySelectorAll('input[name="requirements"]');
            let isValid = true;
            let hasValidRequirements = false;
            
            requirementInputs.forEach((input, index) => {
                const value = input.value.trim();
                
                if (!value) {
                    showError('חובה להזין דרישה', 'requirements');
                    isValid = false;
                } else if (value.length < 3) {
                    showError('דרישה חייבת להכיל לפחות 3 תווים', 'requirements');
                    isValid = false;
                } else {
                    hasValidRequirements = true;
                }
            });
            
            if (hasValidRequirements && isValid) {
                showError('', 'requirements');
            }
            
            return isValid;
        }

        function validateForm() {
            clearErrors();
            
            const isJobTitleValid = validateJobTitle();
            const isLocationValid = validateLocation();
            const isAssignmentTypeValid = validateAssignmentType();
            const isDescriptionValid = validateDescription();
            const isRequirementsValid = validateRequirements();
            
            return isJobTitleValid && isLocationValid && isAssignmentTypeValid && 
                   isDescriptionValid && isRequirementsValid;
        }

        jobTitleSelect.addEventListener('change', function() {
            validateJobTitle();
            toggleOtherInput();
        });

        otherJobTitleInput.addEventListener('input', validateJobTitle);
        locationSelect.addEventListener('change', validateLocation);
        
        document.querySelectorAll('input[name="assignmentType"]').forEach(radio => {
            radio.addEventListener('change', validateAssignmentType);
        });
        
        descriptionTextarea.addEventListener('input', validateDescription);

        document.getElementById('requirementsContainer').addEventListener('input', function(e) {
            if (e.target.name === 'requirements') {
                validateRequirements();
            }
        });

        form.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                
                const firstError = document.querySelector('.client-error:not(:empty)');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });

        function toggleOtherInput() {
            if (jobTitleSelect.value === "אחר") {
                otherJobTitleInput.classList.remove("d-none");
                otherJobTitleInput.value = "";
            } else {
                otherJobTitleInput.value = "";
                otherJobTitleInput.classList.add("d-none");
                showError('', 'otherJobTitleInput');
            }
        }

        toggleOtherInput();

        function updateRemoveButtons() {
            const rows = document.querySelectorAll('#requirementsContainer .input-group');
            rows.forEach((row, index) => {
                const btn = row.querySelector('.remove-requirement');
                const input = row.querySelector('input[name="requirements"]');
                
                if (!input.id) {
                    input.id = `requirement-${index}`;
                }
                
                if (rows.length > 1) {
                    btn.classList.remove('d-none');
                } else {
                    btn.classList.add('d-none');
                }
            });
        }

        document.getElementById('addRequirementBtn').onclick = function() {
            const container = document.getElementById('requirementsContainer');
            const row = document.createElement('div');
            const index = container.children.length;
            
            row.className = 'input-group mb-2';
            row.innerHTML = `
                <input type="text" name="requirements" id="requirement-${index}" placeholder="הכנס דרישת מינימום לתפקיד (לדוגמא - רובאי 03)" class="form-control rounded-3">
                <button type="button" class="btn btn-outline-danger remove-requirement d-none">
                    <i class="bi bi-trash"></i>
                </button>
            `;
            container.appendChild(row);
            updateRemoveButtons();
            
            const newInput = row.querySelector('input[name="requirements"]');
            newInput.addEventListener('input', validateRequirements);
            
            // נקה שגיאות כשמוסיפים דרישה חדשה
            showError('', 'requirements');
        };

        document.getElementById('requirementsContainer').addEventListener('click', function(e) {
            if (e.target.closest('.remove-requirement')) {
                const rows = document.querySelectorAll('#requirementsContainer .input-group');
                if (rows.length > 1) {
                    e.target.closest('.input-group').remove();
                    updateRemoveButtons();
                    
                    // תמיד נקה שגיאות אחרי מחיקה
                    showError('', 'requirements');
                    
                    // אם נשארו דרישות, בדוק אותן מחדש
                    const remainingInputs = document.querySelectorAll('#requirementsContainer input[name="requirements"]');
                    if (remainingInputs.length > 0) {
                        validateRequirements();
                    }
                }
            }
        });

        updateRemoveButtons();
    });
}

positionDom();
