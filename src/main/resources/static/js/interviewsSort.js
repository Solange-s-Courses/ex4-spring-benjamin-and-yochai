document.addEventListener('DOMContentLoaded', function () {
    const sortButtons = document.querySelectorAll("[data-action = 'sort']")

    sortButtons.forEach(btn => {
        btn.addEventListener("click", ()=>{
            const tableId = btn.getAttribute('data-table');
            const colIndex = parseInt(btn.getAttribute('data-col'));
            const sortType = btn.getAttribute('data-sort');
            if (sortType === 'date') {
                genericSortingFunc(tableId, colIndex, sortRowsByDate);
            } else if (sortType === 'role') {
                genericSortingFunc(tableId, colIndex, sortRowsByText);
            }
        })
    })

    function genericSortingFunc(tableId, colIndex, sortingFunc){
        const table = document.getElementById(tableId);
        const tbody = table.tBodies[0];
        const rows = Array.from(tbody.rows);
        const filteredRows = rows.filter(row => row.cells.length > colIndex)
        const isAsc = table.getAttribute('data-sort-date-asc') !== 'true';

        const sortedRows = sortingFunc(filteredRows, colIndex, isAsc);

        sortedRows.forEach(row => tbody.appendChild(row));
        table.setAttribute('data-sort-date-asc', isAsc);

    }

    function sortRowsByDate(filteredRows, colIndex, isAsc) {
        return filteredRows.sort((a, b) => {
            const dateA = new Date(a.cells[colIndex].innerText.split('/').reverse().join('-'));
            const dateB = new Date(b.cells[colIndex].innerText.split('/').reverse().join('-'));
            return isAsc ? dateA - dateB : dateB - dateA;
        });
    }

    function sortRowsByText(filteredRows, colIndex, isAsc) {
        return filteredRows.sort((a, b) => {
            const roleA = a.cells[colIndex].innerText.trim();
            const roleB = b.cells[colIndex].innerText.trim();
            return isAsc ? roleA.localeCompare(roleB) : roleB.localeCompare(roleA);
        });
    }
});

