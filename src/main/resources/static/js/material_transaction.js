// 재료 추가 또는 수정 버튼 클릭 이벤트 핸들러
document.getElementById('addTransactionBtn').addEventListener('click', (event) => {
    event.preventDefault();  // 기본 폼 제출 동작을 막음
    event.stopPropagation();  // 이벤트 전파 방지

    const materialData = {
        transactionDate: document.getElementById('transactionDate').value,
        materialCode: document.getElementById('twoMaterialCode').value,  // materialCode 확인
        companyName: document.getElementById('twoCompanyName').value,
        materialName: document.getElementById('twoMaterialName').value,
        stockIn: parseInt(document.getElementById('stockIn').value, 10),
        stockOut: parseInt(document.getElementById('stockOut').value, 10)
    };
    // 서버로 데이터 전송

    fetch('/inventory_management/addTransaction', {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(materialData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            alert(data.message);  // 서버에서 받은 메시지를 alert에 출력
            loadTransactionList();  // 목록 다시 로딩
        })
        .catch(error => {
            console.error("Error:", error);
            alert(`서버와의 통신 중 오류가 발생했습니다. 상세 내용: ${error.message}`);
        });

});


// 재료 출납 목록 로딩 함수
function loadTransactionList() {
    fetch('/inventory_management/searchTransaction')  // 전체 목록 조회
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
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 출납 목록을 불러오는 중 오류 발생:", error);
        });
}

// 재료 검색 함수
function twoSearch() {
    const materialName = document.getElementById('twoMaterialNameSearch').value.trim();
    const materialCode = document.getElementById('twoMaterialCodeSearch').value.trim();

    let url = `/inventory_management/searchTransaction?`;
    const queryParams = [];

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

// 돋보기 버튼 클릭 시 모달을 띄우는 함수
document.getElementById('materialCompanySelect').addEventListener('click', function() {
    const materialCompanyModal = new bootstrap.Modal(document.getElementById('materialCompanyModal'));
    materialCompanyModal.show(); // 모달을 띄우기
});