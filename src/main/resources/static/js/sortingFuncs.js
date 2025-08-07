/**
 * Generic sorting functions for table operations
 * @module sortingFuncs
 */

/**
 * Generic sorting function that can be used with different sorting algorithms
 * @param {HTMLTableElement} table - The table element to sort
 * @param {number} colIndex - The column index to sort by
 * @param {Function} sortingFunc - The sorting function to use (sortRowsByDate, sortRowsByText, etc.)
 * @param {boolean} activeSorting - Whether to toggle sort direction. Defaults to true
 */
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

/**
 * Sorts table rows by date values in a specific column
 * @param {HTMLTableRowElement[]} filteredRows - Array of table rows to sort
 * @param {number} colIndex - The column index containing date values
 * @param {boolean} isAsc - Whether to sort in ascending order
 * @returns {HTMLTableRowElement[]} Sorted array of table rows
 */
export function sortRowsByDate(filteredRows, colIndex, isAsc) {
    return filteredRows.sort((a, b) => {
        const dateA = new Date(a.cells[colIndex].innerText.split('/').reverse().join('-'));
        const dateB = new Date(b.cells[colIndex].innerText.split('/').reverse().join('-'));
        return isAsc ? dateA - dateB : dateB - dateA;
    });
}

/**
 * Sorts table rows by text values in a specific column
 * @param {HTMLTableRowElement[]} filteredRows - Array of table rows to sort
 * @param {number} colIndex - The column index containing text values
 * @param {boolean} isAsc - Whether to sort in ascending order
 * @returns {HTMLTableRowElement[]} Sorted array of table rows
 */
export function sortRowsByText(filteredRows, colIndex, isAsc) {
    return filteredRows.sort((a, b) => {
        const roleA = a.cells[colIndex].innerText.trim();
        const roleB = b.cells[colIndex].innerText.trim();
        return isAsc ? roleA.localeCompare(roleB) : roleB.localeCompare(roleA);
    });
}
