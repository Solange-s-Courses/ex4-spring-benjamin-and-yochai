function DOM() {
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
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const passwordRegex = /^[A-Za-z0-9!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\?`~]{6,20}$/
    const allowedTypes = ["application/pdf", "image/jpeg", "image/png"];
    // const maxSize = 1024 * 1024;
    let maxSize = parseFileSize(window.maxFileSize || "1MB");
    let valid = true;

    document.addEventListener("DOMContentLoaded", () => {
        const form = document.getElementById("registrationForm");
        const username = document.getElementById("username");
        const email = document.getElementById("email");
        const password = document.getElementById("password");
        const militaryIdDoc = document.getElementById("militaryIdDoc");

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
                    showError("יש להעלות קובץ מסוג PDF, JPG או PNG בלבד", "militaryIdDoc");
                    valid = false;
                }
                else if (file.size > maxSize) {
                    showError("גודל הקובץ חייב להיות עד " + formatFileSize(maxSize), "militaryIdDoc");
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
    });
}
DOM();
