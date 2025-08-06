export function formatDate(isoString) {
    const date = new Date(isoString);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); // Months are zero-indexed
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
}

export function formatTime(isoString){
    const date = new Date(isoString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');

    return `${hours}:${minutes}`;
}

export function formatDateTime(isoString){
    return formatDate(isoString) + ' ' + formatTime(isoString);
}

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