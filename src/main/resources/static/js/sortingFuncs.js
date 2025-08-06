export function genericSortingFunc(table, colIndex, sortingFunc, activeSorting = true){
    const tbody = table.tBodies[0];
    const rows = Array.from(tbody.rows);
    const filteredRows = rows.filter(row => row.cells.length > colIndex)
    let isAsc;

    if(activeSorting) {
        isAsc = table.dataset.sortAsc !== 'true';
        table.setAttribute('data-sort-asc', isAsc);
    }
    else{
        isAsc = table.dataset.sortAsc === "true";
    }

    const sortedRows = sortingFunc(filteredRows, colIndex, isAsc);
    sortedRows.forEach(row => tbody.appendChild(row));
}

export function sortRowsByDate(filteredRows, colIndex, isAsc) {
    return filteredRows.sort((a, b) => {
        const dateA = new Date(a.cells[colIndex].innerText.split('/').reverse().join('-'));
        const dateB = new Date(b.cells[colIndex].innerText.split('/').reverse().join('-'));
        return isAsc ? dateA - dateB : dateB - dateA;
    });
}

export function sortRowsByText(filteredRows, colIndex, isAsc) {
    return filteredRows.sort((a, b) => {
        const roleA = a.cells[colIndex].innerText.trim();
        const roleB = b.cells[colIndex].innerText.trim();
        return isAsc ? roleA.localeCompare(roleB) : roleB.localeCompare(roleA);
    });
}
