// 검색 버튼 클릭 시 호출되는 함수
function oneSearch() {
    const firstRegisterDate = document.getElementById("firstRegisterDate").value;
    const companyName = document.getElementById("oneCompanyName").value.trim();
    const materialName = document.getElementById("oneMaterialName").value.trim();
    const belowSafetyStock = document.getElementById("oneBelowSafetyStock").value === 'yes';
    const stockManagementItem = document.getElementById("oneStockManagementItem").value === 'yes';

    const searchParams = {
        firstRegisterDate,
        companyName,
        materialName,
        belowSafetyStock,
        stockManagementItem
    };

    fetch('/inventory_management/statusSearch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(searchParams)
    })
        .then(response => response.json())
        .then(data => {
            const groupedData = groupBy(data, ['materialCode', 'companyName']);
            updateMaterialStatusTable(groupedData);
        })
        .catch(error => {
            console.error('검색 중 오류 발생:', error);
        });
}

// 데이터를 특정 필드들로 그룹화하는 함수
function groupBy(data, keys) {
    const grouped = {};

    data.forEach(item => {
        const key = keys.map(k => item[k]).join('_');
        if (!grouped[key]) {
            grouped[key] = {
                ...item,
                stockIn: 0,
                stockOut: 0,
                remainingStock: 0
            };
        }

        grouped[key].stockIn += item.stockIn || 0;
        grouped[key].stockOut += item.stockOut || 0;
        grouped[key].remainingStock = grouped[key].stockIn - grouped[key].stockOut;
    });

    return Object.values(grouped);
}

// 재료 목록을 테이블에 업데이트하는 함수
function updateMaterialStatusTable(data) {
    const materialStatusList = document.getElementById('materialStatusList');
    materialStatusList.innerHTML = ''; // 기존 데이터를 초기화합니다.

    if (!data || data.length === 0) {
        const noDataRow = document.createElement('tr');
        noDataRow.innerHTML = '<td colspan="11">현재 등록된 재료가 없습니다.</td>';
        materialStatusList.appendChild(noDataRow);
    } else {
        data.forEach(transaction => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${transaction.firstRegisterDate || 'N/A'}</td>
                <td>${transaction.materialCode || 'N/A'}</td>
                <td>${transaction.materialName || 'N/A'}</td>
                <td>${transaction.materialUnit || 'N/A'}</td>
                <td>${transaction.remainingStock.toLocaleString()}</td>
                <td>${transaction.minQuantity || 'N/A'}</td>
                <td>${transaction.belowSafetyStock ? '예' : '아니오'}</td>
                <td>${transaction.stockManagementItem ? '예' : '아니오'}</td>
                <td>${transaction.companyName || 'N/A'}</td>
            `;
            materialStatusList.appendChild(row);
        });
    }
}

// 초기화 버튼 클릭 시 호출되는 함수
document.querySelector('.btn.btn-secondary').addEventListener('click', function() {
    resetSearchFields();
    oneSearch();  // 초기화 후 전체 데이터를 다시 로드합니다.
});

// 검색 필드를 초기화하는 함수
function resetSearchFields() {
    document.getElementById("firstRegisterDate").value = '';
    document.getElementById("oneCompanyName").value = '';
    document.getElementById("oneMaterialName").value = '';
    document.getElementById("oneBelowSafetyStock").value = '';
    document.getElementById("oneStockManagementItem").value = '';
}



// 재료 목록 로딩 함수 (초기 데이터 로딩 포함)
function loadMaterialStatusList() {
    fetch('/inventory_management/statusSearch', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({})  // 초기 로딩 시 빈 검색 조건으로 전체 데이터 가져옴
    })
        .then(response => response.json())
        .then(data => {
            const groupedData = groupBy(data, ['materialCode', 'companyName']);
            updateMaterialStatusTable(groupedData);
        })
        .catch(error => {
            console.error('재료 목록을 불러오는 중 오류 발생:', error);
        });
}

loadMaterialStatusList();
