import {showToast} from "./toastUtils.js";

document.addEventListener("DOMContentLoaded", () => {
    //users
    const applicationFinalStatus = document.getElementById("applicationFinalStatus");
    const applicationPendingStatus = document.getElementById("applicationPendingStatus");
    const noApplicationStatus = document.getElementById("noApplicationStatus");
    const cancelApplicationBtn = document.getElementById("cancel-application-btn");
    const applyPositionBtn = document.getElementById("apply-position-btn");
    //commander

    const sendReq = async(btn, divToHide, divToShow)=>{
        try{
            btn.disabled = true;
            const response = await fetch(`/restapi/application/${btn.dataset.positionId}/${btn.dataset.action}`,
                {
                    method: "GET"
                });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data = await response.json();
            const msg = data.message;

            divToHide.classList.add("d-none");
            divToShow.classList.remove("d-none");

            showToast(msg);

        }catch (e) {
            const msg = e.message || 'שגיאה בשליחת הבקשה';
            showToast(msg, "danger");
        }finally {
            btn.disabled = false;
        }
    }

    if(cancelApplicationBtn){
        cancelApplicationBtn.addEventListener("click", async ()=>{
            await sendReq(cancelApplicationBtn, applicationPendingStatus, noApplicationStatus);
        })
    }

    if(applyPositionBtn){
        applyPositionBtn.addEventListener("click", async ()=>{
            await sendReq(applyPositionBtn, noApplicationStatus, applicationPendingStatus);
        })
    }

    const getCSRFHeaders = () => {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const headers = { 'Content-Type': 'application/json' };
        
        if (csrfMeta) {
            const csrfToken = csrfMeta.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            headers[csrfHeader] = csrfToken;
        }
        
        return headers;
    };

    // const handleFormSubmission = async (url, options = {}) => {
    //     const {
    //         fallbackErrorMessage = 'אירעה שגיאה',
    //         reloadDelay = 1500,
    //         body = null,
    //         button = null
    //     } = options;
    //
    //     if (button) {
    //         button.disabled = true;
    //     }
    //
    //     try {
    //         const headers = getCSRFHeaders();
    //         const requestOptions = {
    //             method: 'POST',
    //             headers,
    //             credentials: 'include'
    //         };
    //
    //         if (body) {
    //             requestOptions.body = JSON.stringify(body);
    //         }
    //
    //         const response = await fetch(url, requestOptions);
    //         const data = await response.json();
    //
    //         if (response.ok) {
    //             if (data.message) {
    //                 showT oast(data.message, false);
    //             }
    //             setTimeout(() => window.location.reload(), reloadDelay);
    //         } else {
    //             const errorMessage = data.message || fallbackErrorMessage;
    //             showT oast(errorMessage, true);
    //             if (button) {
    //                 button.disabled = false;
    //             }
    //         }
    //     } catch (error) {
    //         showT oast(fallbackErrorMessage, true);
    //         if (button) {
    //             button.disabled = false;
    //         }
    //     }
    // };

    const initShareButton = () => {
        const shareBtn = document.getElementById("shareBtn");
        if (!shareBtn) return;

        shareBtn.addEventListener("click", () => {
            if (navigator.share) {
                navigator.share({
                    title: document.querySelector('h2')?.textContent || '',
                    text: 'בדוק את המשרה הזו:',
                    url: window.location.href
                }).catch(() => {
                    navigator.clipboard.writeText(window.location.href).then(() => {
                        const messageBox = document.getElementById('copy-message');
                        if (messageBox) {
                            messageBox.classList.remove("d-none");
                            setTimeout(() => messageBox.classList.add('d-none'), 3000);
                        }
                    });
                });
            } else {
                navigator.clipboard.writeText(window.location.href).then(() => {
                    const messageBox = document.getElementById('copy-message');
                    if (messageBox) {
                        messageBox.classList.remove("d-none");
                        setTimeout(() => messageBox.classList.add('d-none'), 3000);
                    }
                });
            }
        });
    };

    // OLD FORM HANDLERS - COMMENTED OUT
    /*
    const initFormHandlers = () => {
        // Status change forms
        const statusForms = document.querySelectorAll('form[action*="/positions/"][action*="/status"]');
        statusForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const statusSelect = form.querySelector('select[name="status"]');
                if (!statusSelect) return;
                
                const positionId = form.action.split('/positions/')[1].split('/status')[0];
                const button = form.querySelector('button[type="submit"]');
                
                handleFormSubmission(`/restapi/positions/${positionId}/status`, {
                    loadingText: 'מעדכן...',
                    fallbackErrorMessage: 'אירעה שגיאה בעדכון סטטוס המשרה.',
                    body: { status: statusSelect.value },
                    reloadDelay: 1500,
                    button: button
                });
            });
        });

        // Apply form
        const applyForm = document.querySelector('.apply-form');
        if (applyForm) {
            applyForm.addEventListener('submit', (e) => {
                e.preventDefault();
                const button = applyForm.querySelector('button');
                
                handleFormSubmission(applyForm.action, {
                    loadingText: 'מגיש...',
                    fallbackErrorMessage: 'שגיאה בלתי צפויה.',
                    reloadDelay: 4000,
                    button: button
                });
            });
        }

        // Cancel forms
        const cancelForms = document.querySelectorAll('form[action*="/restapi/"][action*="/cancel"]');
        cancelForms.forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const button = form.querySelector('button[type="submit"]');
                
                handleFormSubmission(form.action, {
                    loadingText: 'מבטל...',
                    fallbackErrorMessage: 'אירעה שגיאה בביטול המועמדות.',
                    reloadDelay: 1500,
                    button: button
                });
            });
        });
    };
    */

    // NEW BUTTON HANDLERS
    // const initButtonHandlers = () => {
    //     // Apply position button
    //     const applyBtn = document.querySelector('.apply-position-btn');
    //     if (applyBtn) {
    //         applyBtn.addEventListener('click', async () => {
    //             const positionId = applyBtn.dataset.positionId;
    //             await handleFormSubmission(`/restapi/${positionId}/apply`, {
    //                 fallbackErrorMessage: 'שגיאה בלתי צפויה.',
    //                 reloadDelay: 4000,
    //                 button: applyBtn
    //             });
    //         });
    //     }
    //
    //     // Cancel application button
    //     const cancelBtn = document.querySelector('.cancel-application-btn');
    //     if (cancelBtn) {
    //         cancelBtn.addEventListener('click', async () => {
    //             const positionId = cancelBtn.dataset.positionId;
    //             await handleFormSubmission(`/restapi/application/${positionId}/cancel`, {
    //                 fallbackErrorMessage: 'אירעה שגיאה בביטול המועמדות.',
    //                 reloadDelay: 1500,
    //                 button: cancelBtn
    //             });
    //         });
    //     }

        // Update status button  -------------------------------------------------------------------------------------------------
        // const updateStatusBtn = document.querySelector('.update-status-btn');
        // if (updateStatusBtn) {
        //     updateStatusBtn.addEventListener('click', async () => {
        //         const positionId = updateStatusBtn.dataset.positionId;
        //         const statusSelect = document.querySelector('.status-select');
        //         if (!statusSelect) return;
        //
        //         await handleFormSubmission(`/restapi/positions/${positionId}/status`, {
        //             fallbackErrorMessage: 'אירעה שגיאה בעדכון סטטוס המשרה.',
        //             body: { status: statusSelect.value },
        //             reloadDelay: 1500,
        //             button: updateStatusBtn
        //         });
        //     });
        // }
//    };

    initShareButton();
    //initButtonHandlers();
});