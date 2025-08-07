/**
 * Position form validation and handling module
 * @module positionForm
 */

/**
 * Initializes position form validation and dynamic functionality
 * Handles job title selection, requirements management, and form validation
 */
function positionDom(){
    document.addEventListener("DOMContentLoaded", function () {
        const jobTitleSelect = document.getElementById("jobTitle");
        const otherJobTitleInput = document.getElementById("otherJobTitleInput");
        const locationSelect = document.getElementById("location");
        const descriptionTextArea = document.getElementById("description");
        const form = document.getElementById('addPositionForm');
        const requirementsContainer = document.getElementById('requirementsContainer');

        /**
         * Clears all validation errors from the form
         */
        function clearErrors() {
            document.querySelectorAll(".client-error").forEach(div => {
                div.textContent = "";
            });
        }

        /**
         * Shows validation error for a specific field
         * @param {string} message - Error message to display
         * @param {string} field - Field name to show error for
         */
        function showError(message, field) {
            const errorDiv = document.querySelector(`[data-error="${field}"]`);
            if (errorDiv) {
                errorDiv.textContent = message;
            }
        }

        /**
         * Validates job title selection and "other" input
         * @returns {boolean} True if validation passes, false otherwise
         */
        function validateJobTitle() {
            const selectedValue = jobTitleSelect.value;
            showError('', 'jobTitle');
            
            if (!selectedValue || selectedValue.trim() === "") {
                showError('חובה לבחור תפקיד', 'jobTitle');
                return false;
            }
            
            if (selectedValue === "אחר") {
                const otherValue = otherJobTitleInput.value;
                if (!otherValue || otherValue.trim() === "") {
                    showError('חובה להזין תפקיד', 'jobTitle');
                    return false;
                }
                if (otherValue.length < 2) {
                    showError('תפקיד חייב להכיל לפחות 2 תווים', 'jobTitle');
                    return false;
                }
            }
            
            return true;
        }

        /**
         * Validates location selection
         * @returns {boolean} True if validation passes, false otherwise
         */
        function validateLocation() {
            const selectedValue = locationSelect.value;
            showError('', 'location');
            
            if (!selectedValue || selectedValue === "") {
                showError('חובה לבחור מיקום', 'location');
                return false;
            }
            
            return true;
        }

        /**
         * Validates assignment type radio button selection
         * @returns {boolean} True if validation passes, false otherwise
         */
        function validateAssignmentType() {
            showError("", 'assignmentType');
            const radioButtons = document.querySelectorAll('input[name="assignmentType"]');
            const selected = Array.from(radioButtons).some(radio => radio.checked);
            
            if (!selected) {
                showError('חובה לבחור סוג שיבוץ', 'assignmentType');
                return false;
            }
            
            return true;
        }

        /**
         * Validates description text area
         * @returns {boolean} True if validation passes, false otherwise
         */
        function validateDescription() {
            showError('', 'description');
            const value = descriptionTextArea.value;
            
            if (!value || value.trim() === "") {
                showError('חובה להזין תיאור תפקיד', 'description');
                return false;
            }
            else if (value.trim().length < 10) {
                showError('תיאור התפקיד חייב להכיל לפחות 10 תווים', 'description');
                return false;
            }
            else if (value.trim().length > 500) {
                showError('תיאור התפקיד לא יכול לעלות על 500 תווים', 'description');
                return false;
            }

            return true;
        }

        /**
         * Validates requirements inputs
         * @returns {boolean} True if validation passes, false otherwise
         */
        function validateRequirements() {
            const requirementInputs = requirementsContainer.querySelectorAll('input');
            let isValid = true;

            requirementInputs.forEach((input) => {
                const value = input.value;
                
                if (!value) {
                    showError('חובה להזין דרישה', 'requirements');
                    isValid = false;
                } else if (value.trim().length < 3) {
                    showError('דרישה חייבת להכיל לפחות 3 תווים', 'requirements');
                    isValid = false;
                }
            });
            
            if (isValid) {
                showError('', 'requirements');
            }
            
            return isValid;
        }

        /**
         * Validates the entire form
         * @returns {boolean} True if all validations pass, false otherwise
         */
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

        // Event listeners for real-time validation
        jobTitleSelect.addEventListener('change', function() {
            validateJobTitle();
            toggleOtherInput();
        });

        otherJobTitleInput.addEventListener('input', validateJobTitle);
        locationSelect.addEventListener('change', validateLocation);
        
        document.querySelectorAll('input[name="assignmentType"]').forEach(radio => {
            radio.addEventListener('change', validateAssignmentType);
        });
        
        descriptionTextArea.addEventListener('input', validateDescription);

        // Form submission handler
        form.addEventListener("submit",  function(e) {
            if (!validateForm()) {
                e.preventDefault();
                
                const firstError = document.querySelector('.client-error:not(:empty)');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });

        /**
         * Toggles visibility of "other" job title input based on selection
         */
        function toggleOtherInput() {
            if (jobTitleSelect.value === "אחר") {
                otherJobTitleInput.classList.remove("d-none");
            } else {
                otherJobTitleInput.value = "";
                otherJobTitleInput.classList.add("d-none");
                showError('', 'otherJobTitleInput');
            }
        }

        toggleOtherInput();

        /**
         * Updates the name attributes of requirement inputs to maintain proper indexing
         */
        function updateRequirementsInputNames() {
            const inputs = requirementsContainer.querySelectorAll('.input-group');
            inputs.forEach((input, index) => {

                const btn = input.querySelector('.remove-requirement');
                const field = input.querySelector('input');
                field.name = `requirements[${index}]`;

                if (inputs.length > 1) {
                    btn.classList.remove('d-none');
                } else {
                    btn.classList.add('d-none');
                }
            });
        }

        // Add requirement button handler
        document.getElementById('addRequirementBtn').addEventListener('click', function () {
            const index = requirementsContainer.querySelectorAll('input').length;
            const div = document.createElement('div');
            div.className = 'input-group mb-2';
            div.innerHTML = `
                <input type="text" name="requirements[${index}]" class="form-control rounded-3" placeholder="הכנס דרישת מינימום לתפקיד">
                <button type="button" class="btn btn-outline-danger remove-requirement">
                    <i class="bi bi-trash"></i>
                </button>
            `;
            requirementsContainer.appendChild(div);
            updateRequirementsInputNames();
            showError('', 'requirements');
            div.querySelector("input").addEventListener("input", validateRequirements)
        });

        // Remove requirement button handler
        requirementsContainer.addEventListener('click', function (e) {
            if (e.target.closest('.remove-requirement')) {
                const inputGroup = e.target.closest('.input-group');
                inputGroup.remove();
                updateRequirementsInputNames();
                showError('', 'requirements');
            }
        });
        updateRequirementsInputNames();
    });
}

positionDom();
