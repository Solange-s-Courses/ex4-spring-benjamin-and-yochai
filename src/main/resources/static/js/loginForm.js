/*document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('loginForm');
    const username = document.getElementById('username');
    const password = document.getElementById('password');
    const usernameError = document.getElementById('usernameError');
    const passwordError = document.getElementById('passwordError');

    if (form) {
        form.addEventListener('submit', function (e) {
            let valid = true;

            // ניקוי הודעות קודמות
            clearErrors();

            // בדיקת שם משתמש
            if (username.value.trim() === '') {
                showError('יש להזין שם משתמש', 'username');
                valid = false;
            }

            // בדיקת סיסמה
            if (password.value.trim() === '') {
                showError('יש להזין סיסמה', 'password');
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
            }
        });
    }

    function clearErrors() {
        username.classList.remove('is-invalid');
        password.classList.remove('is-invalid');
        usernameError.textContent = '';
        passwordError.textContent = '';
    }

    function showError(message, field) {
        if (field === 'username') {
            username.classList.add('is-invalid');
            usernameError.textContent = message;
        } else if (field === 'password') {
            password.classList.add('is-invalid');
            passwordError.textContent = message;
        }
    }
});*/

const loginForm = () => {
    document.addEventListener('DOMContentLoaded', function () {
        const form = document.getElementById('loginForm');
        const username = document.getElementById('username');
        const password = document.getElementById('password');
        const usernameError = document.getElementById('usernameError');
        const passwordError = document.getElementById('passwordError');

        const clearErrors = () => {
            username.classList.remove('is-invalid');
            password.classList.remove('is-invalid');
            usernameError.textContent = '';
            passwordError.textContent = '';
        };

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