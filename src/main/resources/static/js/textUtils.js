/**
 * Text formatting and utility functions module
 * @module textUtils
 */

/**
 * Formats an ISO date string to DD/MM/YYYY format
 * @param {string} isoString - ISO date string to format
 * @returns {string} Formatted date string in DD/MM/YYYY format
 */
export function formatDate(isoString) {
    const date = new Date(isoString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-indexed
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

/**
 * Formats an ISO date string to HH:MM time format
 * @param {string} isoString - ISO date string to format
 * @returns {string} Formatted time string in HH:MM format
 */
export function formatTime(isoString){
    const date = new Date(isoString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${hours}:${minutes}`;
}

/**
 * Formats an ISO date string to DD/MM/YYYY HH:MM format
 * @param {string} isoString - ISO date string to format
 * @returns {string} Formatted date and time string
 */
export function formatDateTime(isoString){
    return formatDate(isoString) + ' ' + formatTime(isoString);
}

/**
 * Gets CSS class and Hebrew text for position status
 * @param {string|Object} status - Position status (enum or string)
 * @returns {Object} Object containing cssClass and text properties
 */
export function getPositionStatusInfo(status) {
    const statusName = status?.name?.() || status; // Handle both enum and string

    const statusMap = {
        'ACTIVE': {
            cssClass: 'bg-success',
            text: 'פעילה'
        },
        'CANCELED': {
            cssClass: 'bg-danger',
            text: 'מבוטלת'
        },
        'FULFILLED': {
            cssClass: 'bg-primary',
            text: 'אוישה'
        },
        'FROZEN': {
            cssClass: 'bg-secondary',
            text: 'מוקפאת'
        }
    };

    return statusMap[statusName] || {
        cssClass: 'bg-light',
        text: statusName
    };
}

/**
 * Gets CSS class and Hebrew text for application status
 * @param {string|Object} status - Application status (enum or string)
 * @returns {Object} Object containing cssClass and text properties
 */
export function getApplicationStatusInfo(status) {
    const statusName = status?.name?.() || status; // Handle both enum and string

    const statusMap = {
        'PENDING': {
            cssClass: 'bg-warning',
            text: 'ממתין'
        },
        'APPROVED': {
            cssClass: 'bg-success',
            text: 'התקבל'
        },
        'REJECTED': {
            cssClass: 'bg-danger',
            text: 'נדחה'
        },
        'CANCELED': {
            cssClass: 'bg-secondary',
            text: 'בוטל'
        }
    };

    return statusMap[statusName] || {
        cssClass: 'bg-light',
        text: statusName
    };
}

/**
 * Gets CSS class and Hebrew text for interview status
 * @param {string|Object} status - Interview status (enum or string)
 * @returns {Object} Object containing cssClass and text properties
 */
export function getInterviewStatusInfo(status) {
    const statusName = status?.name?.() || status; // Handle both enum and string

    const statusMap = {
        'SCHEDULED': {
            cssClass: 'bg-warning',
            text: 'ממתין לאישור'
        },
        'CONFIRMED': {
            cssClass: 'bg-success',
            text: 'מאושר'
        },
        'REJECTED': {
            cssClass: 'bg-danger',
            text: 'נדחה'
        },
        'COMPLETED': {
            cssClass: 'bg-primary',
            text: 'הושלם'
        },
        'CANCELED': {
            cssClass: 'bg-secondary',
            text:'מבוטל'
        }
    };

    return statusMap[statusName] || {
        cssClass: 'bg-light',
        text: statusName
    };
}

/**
 * Converts location enum values to Hebrew text
 * @param {string} location - Location enum value
 * @returns {string} Hebrew text for the location
 */
export function locationEnumToHebrew (location){
    const locationRegionMap = {
        'NORTH': 'צפון',
        'VALLEY': 'בקעה',
        'CENTER': 'מרכז',
        'JERUSALEM_AND_SURROUNDINGS': 'ירושלים והסביבה',
        'JUDEA_AND_SAMARIA': 'יהודה ושומרון',
        'GAZA': 'עזה',
        'SOUTH': 'דרום'
    };

    return locationRegionMap[location] || location;
}