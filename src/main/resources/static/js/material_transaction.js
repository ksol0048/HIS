// 'selectedTransactionId' 변수를 전역에서 한 번만 선언
if (typeof selectedTransactionId === 'undefined') {
    var selectedTransactionId = null;  // 선택된 transactionId 저장
}

// 페이지가 완전히 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    setTodayDate();  // 오늘 날짜 설정
    loadTransactionList();  // 초기 데이터 로딩
});

// 테이블에서 행을 더블클릭했을 때 입력 필드에 값 채우기
function populateTransactionForm(transaction) {
    clearTransactionForm();  // 새로운 선택 시 폼을 초기화

    document.getElementById('transactionId').value = transaction.transactionId || '';  // transactionId 설정
    document.getElementById('stockInDate').value = transaction.stockInDate || '';
    document.getElementById('twoCompanyName').value = transaction.companyName || '';
    document.getElementById('twoMaterialName').value = transaction.materialName || '';
    document.getElementById('twoMaterialCode').value = transaction.materialCode || '';
    document.getElementById('stockIn').value = transaction.stockIn || 0;

    // 필드 수정 불가능하게 설정
    document.getElementById('twoCompanyName').readOnly = true;
    document.getElementById('twoMaterialName').readOnly = true;
    document.getElementById('twoMaterialCode').readOnly = true;

    // 선택된 transactionId 저장
    selectedTransactionId = transaction.transactionId;
}

// 저장, 취소, 삭제 후 입력 필드 비우기
function clearTransactionForm() {
    document.getElementById('transactionId').value = '';
    setTodayDate();  // 오늘 날짜로 다시 설정
    document.getElementById('twoCompanyName').value = '';
    document.getElementById('twoMaterialName').value = '';
    document.getElementById('twoMaterialCode').value = '';
    document.getElementById('stockIn').value = 0;
}

// 필드를 읽기 전용으로 설정하는 함수 (입고일자는 항상 읽기 전용)
function setReadOnlyFields(readOnly) {
    document.getElementById('twoCompanyName').readOnly = readOnly;
    document.getElementById('twoMaterialName').readOnly = readOnly;
    document.getElementById('twoMaterialCode').readOnly = readOnly;
}

// 로딩했을때 최신 날짜로 설정하는 함수
function setTodayDate() {
    const today = new Date().toISOString().split('T')[0];  // 오늘 날짜를 'YYYY-MM-DD' 형식으로 가져옴
    document.getElementById('stockInDate').value = today;  // 입고일자 필드에 오늘 날짜 설정
    document.getElementById('stockOutDate').value = today;  // 출고일자 필드에 오늘 날짜 설정 (필요한 경우)
}

// 출고 날짜를 설정하는 함수
function setStockDate() {
    const today = new Date().toISOString().split('T')[0];  // 오늘 날짜를 'YYYY-MM-DD' 형식으로 가져옴
    document.getElementById('stockOutDate').value = today;  // 출고일자 필드에 오늘 날짜 설정 (필요한 경우)
}

// 페이지가 로드될 때, 그리고 저장/취소/삭제 후에 오늘 날짜로 설정
document.addEventListener('DOMContentLoaded', function() {
    setTodayDate();  // 페이지 로드 시 오늘 날짜 설정
    loadTransactionList();  // 초기 데이터 로딩
    setReadOnlyFields(true);  // 필드 읽기 전용 설정
});

// 트랜잭션 목록 로딩 함수 (목록 불러오기 및 이벤트 재설정 포함)
function loadTransactionList() {
    fetch('/inventory_management/findTransaction')  // 전체 목록 조회
        .then(response => response.json())
        .then(data => {
            // 데이터가 날짜 기준으로 내림차순(최근순)으로 정렬되도록 정렬
            data.sort((a, b) => new Date(b.stockInDate) - new Date(a.stockInDate));

            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = '';  // 기존 테이블 내용 초기화

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 출납 정보가 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.stockInDate || 'N/A'}</td>
                        <td>${transaction.companyName || 'N/A'}</td>
                        <td>${transaction.materialName || 'N/A'}</td>
                        <td>${transaction.materialCode || 'N/A'}</td>
                        <td>${transaction.stockIn != null ? transaction.stockIn.toLocaleString() : 0}</td>
                        <td>${transaction.managerNumber || 'N/A'}</td>
                    `;

                    // 더블클릭 이벤트 추가: 행을 더블클릭하면 데이터를 폼에 채워줌
                    row.addEventListener('dblclick', function () {
                        populateTransactionForm(transaction);  // 폼에 데이터를 채워줌
                    });

                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 출납 목록을 불러오는 중 오류 발생:", error);
        });
}

// 즉시 전체 트랜잭션을 불러오는 함수 호출
loadTransactionList();

// 저장 버튼 클릭 이벤트 핸들러
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
        stockInDate: document.getElementById('stockInDate').value,
        materialCode: document.getElementById('twoMaterialCode').value,
        companyName: document.getElementById('twoCompanyName').value,
        materialName: document.getElementById('twoMaterialName').value,
        stockIn: parseInt(document.getElementById('stockIn').value, 10)
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
            clearTransactionForm();  // 저장 후 필드 비우기
            setReadOnlyFields(true);  // 저장 후 필드 읽기 전용
        })
        .catch(error => {
            console.error("Error:", error);
            alert(`서버와의 통신 중 오류가 발생했습니다. 상세 내용: ${error.message}`);
        });
});

// 취소 버튼 클릭 이벤트 핸들러
document.getElementById('resetTransaction').addEventListener('click', function() {
    clearTransactionForm();  // 필드 초기화
    setTodayDate();  // 오늘 날짜로 다시 설정
    setReadOnlyFields(true);  // 취소 후 필드 읽기 전용
});

// 삭제 버튼 클릭 시 동작
document.getElementById('deleteTransactionBtn').addEventListener('click', function () {
    if (!selectedTransactionId) {
        alert('출납 기록을 선택하세요.');
        return;
    }

    // 삭제 요청 보내기
    fetch(`/inventory_management/deleteTransaction/${selectedTransactionId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                alert('출납 기록이 삭제되었습니다.');
                selectedTransactionId = null;  // 삭제 후 transactionId 초기화
                clearTransactionForm();  // 폼 초기화
                loadTransactionList();  // 삭제 후 목록 새로고침
            } else {
                alert('출납 기록 삭제에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('출납 기록 삭제 중 오류 발생:', error);
        });
});

function twoSearch() {
    const materialName = document.getElementById('twoMaterialNameSearch').value.trim();
    const materialCode = document.getElementById('twoMaterialCodeSearch').value.trim();
    const transactionStartDate = document.getElementById('transactionStartDate').value;
    const transactionEndDate = document.getElementById('transactionEndDate').value;

    let url = `/inventory_management/findTransaction?`;
    const queryParams = [];

    // 쿼리 파라미터에 재료명 추가 (인코딩만 함, %는 서버에서 처리)
    if (materialName) {
        queryParams.push(`materialName=${encodeURIComponent(materialName)}`);
    }

    // 쿼리 파라미터에 재료코드 추가 (인코딩만 함, %는 서버에서 처리)
    if (materialCode) {
        queryParams.push(`materialCode=${encodeURIComponent(materialCode)}`);
    }

    // 쿼리 파라미터에 입출고일자 시작일 추가
    if (transactionStartDate) {
        queryParams.push(`transactionStartDate=${encodeURIComponent(transactionStartDate)}`);
    }

    // 쿼리 파라미터에 입출고일자 종료일 추가
    if (transactionEndDate) {
        queryParams.push(`transactionEndDate=${encodeURIComponent(transactionEndDate)}`);
    }

    // 쿼리 파라미터가 존재할 경우, URL에 붙이기
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
            // 데이터를 최근 날짜순으로 정렬
            data.sort((a, b) => new Date(b.stockInDate) - new Date(a.stockInDate));

            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = ''; // 기존 테이블 내용 초기화

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 출납 내역이 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.stockInDate || 'N/A'}</td>
                        <td>${transaction.companyName || 'N/A'}</td>
                        <td>${transaction.materialName || 'N/A'}</td>
                        <td>${transaction.materialCode || 'N/A'}</td>
                        <td>${transaction.stockIn != null ? transaction.stockIn.toLocaleString() : 0}</td>
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

function resetSearch() {
    // 검색어 초기화
    document.getElementById('twoMaterialNameSearch').value = '';
    document.getElementById('twoMaterialCodeSearch').value = '';

    // 입출고일자 초기화
    document.getElementById('transactionStartDate').value = '';
    document.getElementById('transactionEndDate').value = '';

    // 전체 데이터를 불러오는 요청
    fetch('/inventory_management/reset')  // 전체 데이터를 가져오는 API 엔드포인트
        .then(response => response.json())
        .then(data => {
            // 데이터를 날짜 기준으로 내림차순 정렬 (최근 날짜가 먼저 나오도록)
            data.sort((a, b) => new Date(b.stockInDate) - new Date(a.stockInDate));

            const tbody = document.getElementById('transactionList');
            tbody.innerHTML = '';  // 기존 행 제거

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 재료가 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.stockInDate || 'N/A'}</td>
                        <td>${transaction.companyName || 'N/A'}</td>
                        <td>${transaction.materialName || 'N/A'}</td>
                        <td>${transaction.materialCode || 'N/A'}</td>
                        <td>${transaction.stockIn != null ? transaction.stockIn.toLocaleString() : 0}</td>
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

// 모달 관련 코드 추가

// 업체 목록을 더블클릭했을 때 선택된 업체 정보를 input 필드에 채우기
function selectMaterial(companyName, materialName, materialCode) {
    // 폼 필드에 선택된 값을 넣습니다.
    document.getElementById('twoCompanyName').value = companyName;
    document.getElementById('twoMaterialName').value = materialName;
    document.getElementById('twoMaterialCode').value = materialCode;

    // 모달을 닫습니다.
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

        if (Array.isArray(materialCompanies)) {
            // 목록을 테이블에 추가
            materialCompanies.forEach(material_transactions => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${material_transactions.companyName}</td>
                    <td>${material_transactions.materialName}</td>
                    <td>${material_transactions.materialCode}</td>
                `;

                // 더블클릭 시 선택한 데이터를 폼에 반영
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

// 테이블에서 행을 더블 클릭했을 때 출고관리탭에 선택된 재료 정보를 표시하고 출고 내역을 로드
document.getElementById('transactionList').addEventListener('dblclick', function (event) {
    const targetRow = event.target.closest('tr');

    if (targetRow) {
        const companyName = targetRow.querySelector('td:nth-child(2)').textContent.trim();
        const materialName = targetRow.querySelector('td:nth-child(3)').textContent.trim();
        const materialCode = targetRow.querySelector('td:nth-child(4)').textContent.trim();

        // 선택된 업체명, 재료명, 재료코드를 화면에 표시
        document.getElementById('selectedCompanyName').textContent = companyName;
        document.getElementById('selectedMaterialName').textContent = materialName;
        document.getElementById('selectedMaterialCode').textContent = materialCode;

        // 선택된 값을 입력 필드에도 반영
        document.getElementById('twoCompanyName').value = companyName;
        document.getElementById('twoMaterialName').value = materialName;
        document.getElementById('twoMaterialCode').value = materialCode;

        // 선택된 재료코드로 출고 내역을 로드
        loadOutgoingTransactionList(materialCode);
    }
});


// 모달이 열릴 때마다 재료 목록을 불러오기
document.getElementById('materialCompanyModal').addEventListener('show.bs.modal', fetchMaterialCompanies);

// 재료조회 버튼 클릭 시 모달을 띄우기
document.getElementById('materialCompanySelect').addEventListener('click', function() {
    const materialCompanyModal = new bootstrap.Modal(document.getElementById('materialCompanyModal'));
    materialCompanyModal.show(); // 모달을 띄우기
});


// 초기화 버튼 클릭 이벤트 등록
document.getElementById('twoSearchReset').addEventListener('click', resetSearch);

// 재료 선택 시 출고일자에 오늘 날짜를 설정하는 함수
function populateMaterialAndSetStockOutDate(material) {
    // 기존 재료 정보를 입력 필드에 채워넣는 코드
    document.getElementById('twoCompanyName').value = material.companyName || '';
    document.getElementById('twoMaterialName').value = material.materialName || '';
    document.getElementById('twoMaterialCode').value = material.materialCode || '';

    // 출고일자 필드에 오늘 날짜 설정
    const today = new Date().toISOString().split('T')[0];  // 'YYYY-MM-DD' 형식으로 오늘 날짜 가져오기
    document.getElementById('stockOutDate').value = today;
}

// 더블 클릭 시 재료를 선택하고 출고일자에 오늘 날짜 설정
document.getElementById('transactionList').addEventListener('dblclick', function (event) {
    const targetRow = event.target.closest('tr');
    if (targetRow) {
        const companyName = targetRow.querySelector('td:nth-child(2)').textContent.trim();
        const materialName = targetRow.querySelector('td:nth-child(3)').textContent.trim();
        const materialCode = targetRow.querySelector('td:nth-child(4)').textContent.trim();

        // 선택한 재료 정보를 이용해 폼에 값을 채우고 출고일자를 오늘 날짜로 설정
        const material = {
            companyName: companyName,
            materialName: materialName,
            materialCode: materialCode
        };
        populateMaterialAndSetStockOutDate(material);
    }
});

// 출고 내역 목록 로딩 함수 (재료코드를 기준으로)
function loadOutgoingTransactionList(materialCode) {
    fetch(`/inventory_management/getByMaterialCode?materialCode=${encodeURIComponent(materialCode)}`)
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('outgoingTransactionList');
            tbody.innerHTML = '';  // 기존 테이블 내용 초기화

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="2">현재 등록된 출고 내역이 없습니다.</td></tr>';
            } else {
                data.forEach(transaction => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${transaction.stockOutDate || 'N/A'}</td>
                        <td>${transaction.stockOut != null ? transaction.stockOut.toLocaleString() : 0}</td>
                    `;

                    // 더블클릭 이벤트 추가
                    row.addEventListener('dblclick', function () {
                        populateOutgoingTransactionForm(transaction); // 더블클릭 시 데이터 입력 필드에 채우기
                    });

                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error('출고 내역을 불러오는 중 오류 발생:', error);
        });
}

// 출고 데이터를 입력 필드로 채우는 함수
function populateOutgoingTransactionForm(transaction) {
    // 입력 필드에 선택된 출고 데이터를 채움
    document.getElementById('stockOutDate').value = transaction.stockOutDate || ''; // 출고일자
    document.getElementById('stockOut').value = transaction.stockOut || 0; // 출고량
    document.getElementById('stockOutId').value = transaction.stockOutId || ''; // stockOutId를 hidden 필드에 저장
}

// 페이지 로드 시 출고 데이터 리스트 로딩
loadOutgoingTransactionList();

// 저장 버튼 클릭 이벤트 핸들러
document.getElementById('saveOutTransactionBtn').addEventListener('click', function (event) {
    event.preventDefault();
    event.stopPropagation();

    const stockOutId = document.getElementById('stockOutId').value;
    const stockOutDate = document.getElementById('stockOutDate').value;
    const stockOut = document.getElementById('stockOut').value;
    const materialCode = document.getElementById('selectedMaterialCode').textContent;

    let url, method;

    // 수정인지 신규 저장인지 확인
    if (stockOutId) {
        url = `/inventory_management/updateStockTransaction`;
        method = "PUT";
    } else {
        url = `/inventory_management/addStockTransaction`;
        method = "POST";
    }

    const stockOutData = {
        stockOutId: stockOutId,
        stockOutDate: stockOutDate,
        stockOut: parseInt(stockOut, 10),
        materialCode: materialCode
    };

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(stockOutData)
    })
        .then(response => response.json())
        .then(data => {
            if (!data.success) {
                alert(data.message);  // 재고량 초과 시 메시지 표시
            } else {
                alert(data.message);  // 성공 메시지 표시
                clearStockTransactionForm(); // 폼 초기화
                setStockDate();  // 오늘 날짜로 다시 설정
                loadOutgoingTransactionList(materialCode);  // 목록 다시 로드

                document.getElementById('stockOutId').value = '';  // ID 초기화
            }
        })
        .catch(error => {
            console.error("수정 오류:", error);
            alert(`수정 중 오류가 발생했습니다. 상세 내용: ${error.message}`);
        });
});





// 삭제 버튼 클릭 시 동작하는 함수
document.getElementById('deleteStockTransactionBtn').addEventListener('click', function () {
    const stockOutId = document.getElementById('stockOutId').value;  // 삭제할 출고 내역의 ID

    // stockOutId가 있는지 확인
    if (!stockOutId) {
        alert('삭제할 출고 기록을 선택하세요.');
        return;
    }

    // 삭제 요청 보내기
    fetch(`/inventory_management/deleteStockOut/${stockOutId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                alert('출고 기록이 삭제되었습니다.');
                clearStockTransactionForm();  // 출고 필드만 초기화
                setStockDate();
                loadOutgoingTransactionList(document.getElementById('selectedMaterialCode').textContent);  // 삭제 후에도 재료 목록 다시 로딩
            } else {
                throw new Error('출고 기록 삭제에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('출고 기록 삭제 중 오류 발생:', error);
            alert(`출고 기록 삭제 중 오류가 발생했습니다: ${error.message}`);
        });
});

// 취소 버튼 클릭 시 선택한 출고 내역을 초기화하는 함수
document.getElementById('resetStockTransaction').addEventListener('click', function () {
    clearStockTransactionForm();  // 출고 필드 초기화
    setStockDate();  // 오늘 날짜로 다시 설정
    selectedTransactionId = null;  // 기존 선택된 ID 초기화 (신규 저장 모드로 전환)
});

// 필드를 초기화하는 함수
function clearStockTransactionForm() {
    document.getElementById('stockOutDate').value = '';
    document.getElementById('stockOut').value = '';
    document.getElementById('transactionId').value = '';
    document.getElementById('stockOutId').value = '';
}




