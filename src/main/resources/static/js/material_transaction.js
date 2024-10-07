// 페이지가 완전히 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    setTodayDate();  // 오늘 날짜 설정
    loadTransactionList();  // 초기 데이터 로딩
});

document.getElementById('addTransactionBtn').addEventListener('click', (event) => {
    event.preventDefault();
    event.stopPropagation();

    const transactionIdElement = document.getElementById('transactionId');
    const transactionId = transactionIdElement ? transactionIdElement.value : null;

    let url, method;
    if (transactionId) {
        url = `/inventory_management/updateTransaction`;
        method = "PUT";
    } else {
        url = `/inventory_management/addTransaction`;
        method = "POST";
    }

    const transactionData = {
        transactionId: transactionId,
        transactionDate: document.getElementById('transactionDate').value,
        materialCode: document.getElementById('twoMaterialCode').value,
        companyName: document.getElementById('twoCompanyName').value,
        materialName: document.getElementById('twoMaterialName').value,
        stockIn: parseInt(document.getElementById('stockIn').value, 10),
        stockOut: parseInt(document.getElementById('stockOut').value, 10)
    };

    fetch(url, {
        method: method,
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(transactionData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            alert(data.message);
            loadTransactionList();  // 목록 다시 로딩
        })
        .catch(error => {
            console.error("Error:", error);
            alert(`서버와의 통신 중 오류가 발생했습니다. 상세 내용: ${error.message}`);
        });
});


// 재료 출납 목록 로딩 함수 (테이블 행 더블클릭 이벤트 추가)
function loadTransactionList() {
    fetch('/inventory_management/findTransaction')  // 전체 목록 조회
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = '';  // 기존 테이블 내용 초기화

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 출납 정보가 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.transactionDate || 'N/A'}</td>
                        <td>${transaction.companyName || 'N/A'}</td>
                        <td>${transaction.materialName || 'N/A'}</td>
                        <td>${transaction.materialCode || 'N/A'}</td>
                        <td>${transaction.stockIn != null ? transaction.stockIn.toLocaleString() : 'N/A'}</td>
                        <td>${transaction.stockOut != null ? transaction.stockOut.toLocaleString() : 'N/A'}</td>
                        <td>${transaction.managerNumber || 'N/A'}</td>
                    `;

                    // 더블클릭 이벤트 추가: 행을 더블클릭하면 데이터를 폼에 채워줌
                    row.addEventListener('dblclick', function () {
                        populateTransactionForm(transaction);
                    });

                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 출납 목록을 불러오는 중 오류 발생:", error);
        });
}

// 테이블 행 더블클릭 이벤트 추가 시 transactionId 설정 및 필드 읽기 전용 처리
function populateTransactionForm(transaction) {
    document.getElementById('transactionId').value = transaction.transactionId || '';  // transactionId 설정
    document.getElementById('transactionDate').value = transaction.transactionDate || '';
    document.getElementById('twoCompanyName').value = transaction.companyName || '';
    document.getElementById('twoMaterialName').value = transaction.materialName || '';
    document.getElementById('twoMaterialCode').value = transaction.materialCode || '';
    document.getElementById('stockIn').value = transaction.stockIn || 0;
    document.getElementById('stockOut').value = transaction.stockOut || 0;

    // 수정할 수 없도록 필드를 읽기 전용으로 설정
    document.getElementById('transactionDate').readOnly = true;
    document.getElementById('twoCompanyName').readOnly = true;
    document.getElementById('twoMaterialName').readOnly = true;
    document.getElementById('twoMaterialCode').readOnly = true;
}

// 재료 조회 후 취소 버튼 클릭 시 필드 읽기 전용 해제
document.getElementById('resetTransaction').addEventListener('click', function() {
    // transactionId를 초기화하고, 필드들을 다시 수정 가능하게 설정
    document.getElementById('transactionId').value = '';
    document.getElementById('transactionDate').readOnly = false;
    document.getElementById('twoCompanyName').readOnly = false;
    document.getElementById('twoMaterialName').readOnly = false;
    document.getElementById('twoMaterialCode').readOnly = false;
});


// 페이지 로드 시 재료 출납 목록을 불러오는 함수 호출
loadTransactionList();  // 초기 데이터 로딩


// 검색 함수
function twoSearch() {
    const materialName = document.getElementById('twoMaterialNameSearch').value.trim();
    const materialCode = document.getElementById('twoMaterialCodeSearch').value.trim();
    const transactionStartDate = document.getElementById('transactionStartDate').value;
    const transactionEndDate = document.getElementById('transactionEndDate').value;

    let url = `/inventory_management/findTransaction?`;
    const queryParams = [];

    if (transactionStartDate) queryParams.push(`transactionStartDate=${encodeURIComponent(transactionStartDate)}`);
    if (transactionEndDate) queryParams.push(`transactionEndDate=${encodeURIComponent(transactionEndDate)}`);
    if (materialName) queryParams.push(`materialName=${encodeURIComponent(materialName)}`);
    if (materialCode) queryParams.push(`materialCode=${encodeURIComponent(materialCode)}`);

    if (queryParams.length > 0) {
        url += queryParams.join('&');
    }

    // 서버에 fetch 요청
    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`서버 응답 오류: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = ''; // 기존 테이블 내용 초기화

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 출납 내역이 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.transactionDate || 'N/A'}</td>
                        <td>${transaction.companyName || 'N/A'}</td>
                        <td>${transaction.materialName || 'N/A'}</td>
                        <td>${transaction.materialCode || 'N/A'}</td>
                        <td>${transaction.stockIn != null ? transaction.stockIn.toLocaleString() : 'N/A'}</td>
                        <td>${transaction.stockOut != null ? transaction.stockOut.toLocaleString() : 'N/A'}</td>
                        <td>${transaction.managerNumber || 'N/A'}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("검색 중 오류 발생:", error);
            alert(`검색 중 오류가 발생했습니다. 자세한 내용: ${error.message}`);
        });
}

// 초기화 버튼 클릭 시 호출되는 함수
function resetSearch() {
    // 검색어 초기화
    document.getElementById('twoMaterialNameSearch').value = '';
    document.getElementById('twoMaterialCodeSearch').value = '';

    // 전체 데이터를 불러오는 요청
    fetch('/inventory_management/searchTransaction')  // 전체 데이터를 가져오는 API 엔드포인트
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = '';  // 기존 행 제거

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 재료가 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${material_transactions.transactionDate || 'N/A'}</td>
                        <td>${material_transactions.companyName || 'N/A'}</td>
                        <td>${material_transactions.materialName || 'N/A'}</td>
                        <td>${material_transactions.materialCode || 'N/A'}</td>
                        <td>${material_transactions.stockIn != null ? transaction.stockIn.toLocaleString() : 'N/A'}</td>
                        <td>${material_transactions.stockOut != null ? transaction.stockOut.toLocaleString() : 'N/A'}</td>
                        <td>${material_transactions.managerNumber || 'N/A'}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 출납 목록을 불러오는 중 오류 발생:", error);
        });
}

// 페이지 로드 시 재료 출납 목록을 불러오는 함수 호출
loadTransactionList();  // 초기 데이터 로딩

// 업체 목록을 더블클릭했을 때 선택된 업체 정보를 input 필드에 채우는 함수
function selectMaterial(companyName, materialName, materialCode) {
    // 선택한 업체 정보를 input 필드에 채우기
    document.getElementById('twoCompanyName').value = companyName;
    document.getElementById('twoMaterialName').value = materialName;
    document.getElementById('twoMaterialCode').value = materialCode;

    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('materialCompanyModal'));
    modal.hide();
}

// 서버에서 재료 목록을 불러오는 함수
async function fetchMaterialCompanies() {
    try {
        const response = await fetch('/inventory_management/searchMaterials'); // 서버에서 업체 목록을 가져오는 API 호출
        const materialCompanies = await response.json();

        const materialCompanyList = document.getElementById('materialCompanyList');
        materialCompanyList.innerHTML = ''; // 기존 목록 초기화

        // 서버에서 받은 데이터가 배열인지 확인
        if (Array.isArray(materialCompanies)) {
            // 배열일 경우 데이터를 테이블에 추가
            materialCompanies.forEach(material_transactions => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${material_transactions.companyName}</td>
                    <td>${material_transactions.materialName}</td>
                    <td>${material_transactions.materialCode}</td>
                `;

                // 행을 더블클릭했을 때 업체 선택 함수 호출
                row.addEventListener('dblclick', function() {
                    selectMaterial(
                        material_transactions.companyName,
                        material_transactions.materialName,
                        material_transactions.materialCode
                    );
                });

                materialCompanyList.appendChild(row);
            });
        } else {
            console.error("서버에서 배열이 아닌 데이터가 반환되었습니다.");
        }
    } catch (error) {
        console.error("재료 목록을 불러오는 중 오류 발생:", error);
    }
}

// 모달이 열릴 때마다 업체 목록을 불러오기
document.getElementById('materialCompanyModal').addEventListener('show.bs.modal', fetchMaterialCompanies);

// 재료조회 버튼 클릭 시 모달을 띄우는 함수
document.getElementById('materialCompanySelect').addEventListener('click', function() {
    const materialCompanyModal = new bootstrap.Modal(document.getElementById('materialCompanyModal'));
    materialCompanyModal.show(); // 모달을 띄우기
});

// 취소 버튼 클릭 시 transactionId 초기화
document.getElementById('resetTransaction').addEventListener('click', function() {
    document.getElementById('transactionId').value = '';  // transactionId 초기화
});
