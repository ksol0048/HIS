loadMaterialStatus();

// 재료 현황 전체 데이터 로드 함수
function loadMaterialStatus() {
    // 서버로 전체 데이터를 불러오는 요청
    fetch('/inventory_management/materialStatus/search')  // 쿼리 파라미터 없이 전체 데이터를 요청
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답이 올바르지 않습니다.');
            }
            return response.json();
        })
        .then(data => {
            console.log('데이터 불러오기 성공:', data);  // 불러온 데이터 콘솔에 출력
            updateMaterialStatusTable(data);  // 받은 데이터를 테이블에 업데이트
        })
        .catch(error => {
            console.error('전체 재료 현황 데이터를 불러오는 중 오류 발생:', error);
        });
}


// 검색 버튼 클릭 시 실행되는 함수
function oneSearch() {
    // 검색 필드에서 값 가져오기
    const firstRegisterDate = document.getElementById('firstRegisterDate').value;
    const companyName = document.getElementById('oneCompanyName').value.trim();
    const materialName = document.getElementById('oneMaterialName').value.trim();
    const belowSafetyStock = document.getElementById('oneBelowSafetyStock').value;
    const stockManagementItem = document.getElementById('oneStockManagementItem').value;

    // 쿼리 파라미터 생성
    let queryParams = new URLSearchParams();
    if (firstRegisterDate) queryParams.append('firstRegisterDate', firstRegisterDate);
    if (companyName) queryParams.append('companyName', companyName);
    if (materialName) queryParams.append('materialName', materialName);
    if (belowSafetyStock) queryParams.append('belowSafetyStock', belowSafetyStock);
    if (stockManagementItem) queryParams.append('stockManagementItem', stockManagementItem);

    // 검색 조건을 서버로 요청
    fetch('/inventory_management/materialStatus/search?' + queryParams.toString())
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답이 올바르지 않습니다.');
            }
            return response.json();
        })
        .then(data => {
            updateMaterialStatusTable(data);  // 받은 데이터로 테이블 업데이트
        })
        .catch(error => {
            console.error('재료 현황 검색 중 오류 발생:', error);
        });
}

// 테이블 업데이트 함수
function updateMaterialStatusTable(data) {
    const materialStatusList = document.getElementById('materialStatusList');
    materialStatusList.innerHTML = '';  // 기존 테이블 내용 초기화

    if (data.length === 0) {
        materialStatusList.innerHTML = '<tr><td colspan="9">현재 등록된 재료가 없습니다.</td></tr>';
        return;
    }

    // 데이터를 테이블에 추가
    data.forEach(material => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${material.firstRegisterDate || 'N/A'}</td>
            <td>${material.materialCode || 'N/A'}</td>
            <td>${material.materialName || 'N/A'}</td>
            <td>${material.materialUnit || 'N/A'}</td>
            <td>${material.remainingStock != null ? material.remainingStock : 0}</td>
            <td>${material.minQuantity || 0}</td>
            <td>${material.belowSafetyStock ? '예' : '아니오'}</td>
            <td>${material.stockManagementItem ? '예' : '아니오'}</td>
            <td>${material.companyName || 'N/A'}</td>
        `;
        materialStatusList.appendChild(row);
    });
}

// 초기화 버튼 클릭 시, 모든 필드를 초기화하고 전체 목록을 다시 로드
document.querySelector('button[type="reset"]').addEventListener('click', function () {
    document.getElementById('firstRegisterDate').value = '';
    document.getElementById('oneCompanyName').value = '';
    document.getElementById('oneMaterialName').value = '';
    document.getElementById('oneBelowSafetyStock').value = '';
    document.getElementById('oneStockManagementItem').value = '';

    loadMaterialStatus();  // 초기화 후 전체 목록 다시 로드
});
