/*document.addEventListener("DOMContentLoaded", () => {
    function parseFileSize(size) {
        if (typeof size === "number") return size;
        if (size.endsWith("MB")) {
            return parseInt(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return parseInt(size.replace("KB", "")) * 1024;
        } else {
            return parseInt(size);
        }
    }
    function formatFileSize(bytes) {
        if (bytes >= 1024 * 1024) {
            return (bytes / (1024 * 1024)) + "MB";
        } else if (bytes >= 1024) {
            return (bytes / 1024) + "KB";
        } else {
            return bytes + "B";
        }
    }
    const usernameRegex = /^[A-Za-z0-9]{3,20}$/
    const hebrewNameRegex = /^[\u05D0-\u05EA]{3,20}$/
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const passwordRegex = /^[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\?`~]{6,20}$/
    const allowedTypes = ["application/pdf"];
    let maxSize = parseFileSize(window.maxFileSize || "1MB");
    let valid = true;

    const form = document.getElementById("registrationForm");
    const username = document.getElementById("username");
    const firstName = document.getElementById("firstName");
    const lastName = document.getElementById("lastName");
    const email = document.getElementById("email");
    const password = document.getElementById("password");
    const militaryIdDoc = document.getElementById("militaryIdDoc");
    const aboutTextarea = document.getElementById('about');
    const charCount = document.getElementById('charCount');

    if (form) {
        form.addEventListener("submit", (event) => {
            valid = true;
            clearErrors();

            if (!username.value || username.value.trim().length === 0) {
                showError("יש להזין שם משתמש", "username");
                valid = false;
            }
            else if(!usernameRegex.test(username.value.trim())){
                showError("על שם המשתמש להכיל תווים באנגלית ומספרים בלבד, באורך 3-20", "username");
                valid = false;
            }

            if (!firstName.value || firstName.value.trim().length === 0) {
                showError("יש להזין שם פרטי", "firstName");
                valid = false;
            }
            else if(!hebrewNameRegex.test(firstName.value.trim())){
                showError("על השם הפרטי להכיל תווים בעברית בלבד, באורך 3-20", "firstName");
                valid = false;
            }

            if (!lastName.value || lastName.value.trim().length === 0) {
                showError("יש להזין שם משפחה", "lastName");
                valid = false;
            }
            else if(!hebrewNameRegex.test(lastName.value.trim())){
                showError("על שם המשפחה להכיל תווים בעברית בלבד, באורך 3-20", "lastName");
                valid = false;
            }

            if (!email.value || email.value.trim().length === 0) {
                showError("יש להזין כתובת מייל", "email");
                valid = false;
            }
            else if(!emailRegex.test(email.value.trim())){
                showError("יש להזין כתובת מייל תקינה", "email");
                valid = false;
            }

            if (!password.value || password.value.trim().length === 0) {
                showError("יש להזין סיסמה", "password");
                valid = false;
            }
            else if(!passwordRegex.test(password.value.trim())){
                showError("יש להזין סיסמה באורך 6-20, המכילה תוים באנגלית, ספרות ותווים מיוחדים", "password");
                valid = false;
            }

            const file = militaryIdDoc.files[0];

            if (!file) {
                showError("חובה לצרף קובץ", "militaryIdDoc");
                valid = false;
            }
            else if (!allowedTypes.includes(file.type)) {
                showError("יש להעלות קובץ מסוג PDF בלבד", "militaryIdDoc");
                valid = false;
            }
            else if (file.size > maxSize) {
                showError("גודל הקובץ חייב להיות עד " + formatFileSize(maxSize), "militaryIdDoc");
                valid = false;
            }

            // ולידציה לשדה about
            if (!aboutTextarea.value || aboutTextarea.value.trim().length === 0) {
                showError("חובה לספר על עצמך", "about");
                valid = false;
            }

            if (!valid){
                event.preventDefault();
            }

        });
    }

    function clearErrors() {
        document.querySelectorAll(".client-error, .server-error").forEach(div=>{
            div.textContent = "";
        });
    }

    function showError(message, field) {
        const errorDiv = document.querySelector(`[data-error="${field}"]`);
        if (errorDiv) {
            errorDiv.textContent = message;
        }
    }

    if (aboutTextarea && charCount) {
        aboutTextarea.addEventListener('input', function() {
            const currentLength = this.value.length;
            charCount.textContent = currentLength;
            charCount.classList.remove('text-danger', 'text-warning', 'text-muted');
            if (currentLength > 450) {
                charCount.classList.add('text-danger');
            } else if (currentLength > 400) {
                charCount.classList.add('text-warning');
            } else {
                charCount.classList.add('text-muted');
            }
        });
    }
});
*/

const registrationForm = () => {
    document.addEventListener("DOMContentLoaded", () => {
        const parseFileSize = (size) => {
            if (typeof size === "number") return size;
            if (size.endsWith("MB")) {
                return parseInt(size.replace("MB", "")) * 1024 * 1024;
            } else if (size.endsWith("KB")) {
                return parseInt(size.replace("KB", "")) * 1024;
            } else {
                return parseInt(size);
            }
        };

        const formatFileSize = (bytes) => {
            if (bytes >= 1024 * 1024) {
                return (bytes / (1024 * 1024)) + "MB";
            } else if (bytes >= 1024) {
                return (bytes / 1024) + "KB";
            } else {
                return bytes + "B";
            }
        };

        const clearErrors = () => {
            document.querySelectorAll(".client-error, .server-error").forEach(div=>{
                div.textContent = "";
            });
        };

        const showError = (message, field) => {
            const errorDiv = document.querySelector(`[data-error="${field}"]`);
            if (errorDiv) {
                errorDiv.textContent = message;
            }
        };

        const usernameRegex = /^[A-Za-z0-9]{3,20}$/;
        const hebrewNameRegex = /^[\u05D0-\u05EA]{3,20}$/;
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const passwordRegex = /^[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\?`~]{6,20}$/;
        const allowedTypes = ["application/pdf"];
        let maxSize = parseFileSize(window.maxFileSize || "1MB");
        let valid = true;

        const form = document.getElementById("registrationForm");
        const username = document.getElementById("username");
        const firstName = document.getElementById("firstName");
        const lastName = document.getElementById("lastName");
        const email = document.getElementById("email");
        const password = document.getElementById("password");
        const militaryIdDoc = document.getElementById("militaryIdDoc");
        const aboutTextarea = document.getElementById('about');
        const charCount = document.getElementById('charCount');

        if (form) {
            form.addEventListener("submit", (event) => {
                valid = true;
                clearErrors();

                if (!username.value || username.value.trim().length === 0) {
                    showError("יש להזין שם משתמש", "username");
                    valid = false;
                }
                else if(!usernameRegex.test(username.value.trim())){
                    showError("על שם המשתמש להכיל תווים באנגלית ומספרים בלבד, באורך 3-20", "username");
                    valid = false;
                }

                if (!firstName.value || firstName.value.trim().length === 0) {
                    showError("יש להזין שם פרטי", "firstName");
                    valid = false;
                }
                else if(!hebrewNameRegex.test(firstName.value.trim())){
                    showError("על השם הפרטי להכיל תווים בעברית בלבד, באורך 3-20", "firstName");
                    valid = false;
                }

                if (!lastName.value || lastName.value.trim().length === 0) {
                    showError("יש להזין שם משפחה", "lastName");
                    valid = false;
                }
                else if(!hebrewNameRegex.test(lastName.value.trim())){
                    showError("על שם המשפחה להכיל תווים בעברית בלבד, באורך 3-20", "lastName");
                    valid = false;
                }

                if (!email.value || email.value.trim().length === 0) {
                    showError("יש להזין כתובת מייל", "email");
                    valid = false;
                }
                else if(!emailRegex.test(email.value.trim())){
                    showError("יש להזין כתובת מייל תקינה", "email");
                    valid = false;
                }

                if (!password.value || password.value.trim().length === 0) {
                    showError("יש להזין סיסמה", "password");
                    valid = false;
                }
                else if(!passwordRegex.test(password.value.trim())){
                    showError("יש להזין סיסמה באורך 6-20, המכילה תוים באנגלית, ספרות ותווים מיוחדים", "password");
                    valid = false;
                }

                const file = militaryIdDoc.files[0];

                if (!file) {
                    showError("חובה לצרף קובץ", "militaryIdDoc");
                    valid = false;
                }
                else if (!allowedTypes.includes(file.type)) {
                    showError("יש להעלות קובץ מסוג PDF בלבד", "militaryIdDoc");
                    valid = false;
                }
                else if (file.size > maxSize) {
                    showError("גודל הקובץ חייב להיות עד " + formatFileSize(maxSize), "militaryIdDoc");
                    valid = false;
                }

                // ולידציה לשדה about
                if (!aboutTextarea.value || aboutTextarea.value.trim().length === 0) {
                    showError("חובה לספר על עצמך", "about");
                    valid = false;
                }

                if (!valid){
                    event.preventDefault();
                }
            });
        }

        if (aboutTextarea && charCount) {
            aboutTextarea.addEventListener('input', function() {
                const currentLength = this.value.length;
                charCount.textContent = currentLength;
                charCount.classList.remove('text-danger', 'text-warning', 'text-muted');
                if (currentLength > 450) {
                    charCount.classList.add('text-danger');
                } else if (currentLength > 400) {
                    charCount.classList.add('text-warning');
                } else {
                    charCount.classList.add('text-muted');
                }
            });
        }
    });
};

registrationForm();