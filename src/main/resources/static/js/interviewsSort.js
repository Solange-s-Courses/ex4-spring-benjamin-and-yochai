document.addEventListener('DOMContentLoaded', function () {
    document.body.addEventListener('click', function (e) {
        if (e.target.closest('.sort-link')) {
            e.preventDefault();
            const link = e.target.closest('.sort-link');
            const tableId = link.getAttribute('data-table');
            const colIndex = parseInt(link.getAttribute('data-col'));
            const sortType = link.getAttribute('data-sort');
            if (sortType === 'date') {
                sortTableByDate(tableId, colIndex);
            } else if (sortType === 'role') {
                sortTableByRole(tableId, colIndex);
            }
        }
    });
});

function sortTableByDate(tableId, colIndex) {
    const table = document.getElementById(tableId);
    const tbody = table.tBodies[0];
    const rows = Array.from(tbody.rows);
    const isAsc = table.getAttribute('data-sort-date-asc') !== 'true';
    rows.sort((a, b) => {
        const dateA = new Date(a.cells[colIndex].innerText.split('/').reverse().join('-'));
        const dateB = new Date(b.cells[colIndex].innerText.split('/').reverse().join('-'));
        return isAsc ? dateA - dateB : dateB - dateA;
    });
    rows.forEach(row => tbody.appendChild(row));
    table.setAttribute('data-sort-date-asc', isAsc);
}

function sortTableByRole(tableId, colIndex) {
    const table = document.getElementById(tableId);
    const tbody = table.tBodies[0];
    const rows = Array.from(tbody.rows);
    const isAsc = table.getAttribute('data-sort-role-asc') !== 'true';
    rows.sort((a, b) => {
        const roleA = a.cells[colIndex].innerText.trim();
        const roleB = b.cells[colIndex].innerText.trim();
        return isAsc ? roleA.localeCompare(roleB) : roleB.localeCompare(roleA);
    });
    rows.forEach(row => tbody.appendChild(row));
    table.setAttribute('data-sort-role-asc', isAsc);
} 