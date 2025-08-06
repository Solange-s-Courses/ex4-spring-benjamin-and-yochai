import {genericSortingFunc, sortRowsByDate, sortRowsByText} from "./sortingFuncs.js";

const sortingDom = ()=>{
    document.addEventListener('DOMContentLoaded', function () {
        const sortButtons = document.querySelectorAll("[data-action = 'sort']")

        sortButtons.forEach(btn => {
            btn.addEventListener("click", ()=>{
                const table = document.getElementById(btn.getAttribute('data-table'));
                const colIndex = parseInt(btn.getAttribute('data-col'));
                const sortType = btn.getAttribute('data-sort');
                if (sortType === 'date') {
                    genericSortingFunc(table, colIndex, sortRowsByDate);
                } else if (sortType === 'role') {
                    genericSortingFunc(table, colIndex, sortRowsByText);
                }
                table.setAttribute('data-sort-type', sortType);
            })
        })

    });
}

sortingDom();


