/**
 * Login form validation and handling module
 * @module loginForm
 */

/**
 * Initializes login form validation and event handling
 * Sets up form submission validation for username and password fields
 */
const loginForm = () => {
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('loginForm');
        const username = document.getElementById('username');
        const password = document.getElementById('password');
        const usernameError = document.getElementById('usernameError');
        const passwordError = document.getElementById('passwordError');

        /**
         * Clears all validation errors from the form
         */
        const clearErrors = () => {
            username.classList.remove('is-invalid');
            password.classList.remove('is-invalid');
            usernameError.textContent = '';
            passwordError.textContent = '';
        };

        /**
         * Shows validation error for a specific field
         * @param {string} message - Error message to display
         * @param {string} field - Field name ('username' or 'password')
         */
        const showError = (message, field) => {
            if (field === 'username') {
                username.classList.add('is-invalid');
                usernameError.textContent = message;
            } else if (field === 'password') {
                password.classList.add('is-invalid');
                passwordError.textContent = message;
            }
        };

        if (form) {
            form.addEventListener('submit', function (e) {
                let valid = true;

                clearErrors();

                if (username.value.trim() === '') {
                    showError('יש להזין שם משתמש', 'username');
                    valid = false;
                }

                if (password.value.trim() === '') {
                    showError('יש להזין סיסמה', 'password');
                    valid = false;
                }

                if (!valid) {
                    e.preventDefault();
                }
            });
        }
    });
};

loginForm();