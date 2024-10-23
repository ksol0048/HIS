

// 재료 정보를 폼에 채우는 함수 (수정 모드)
function populateMaterialForm(material) {
    document.getElementById('threeMaterialCode').value = material.materialCode;
    document.getElementById('threeMaterialCode').readOnly = true;  // materialCode는 수정할 수 없도록 설정
    document.getElementById('threeMaterialName').value = material.materialName;
    document.getElementById('materialUnit').value = material.materialUnit;
    document.getElementById('materialUnitPrice').value = material.materialUnitPrice;
    document.getElementById('minQuantity').value = material.minQuantity;

    // 재고관리여부 설정
    const stockManagementItemElement = document.getElementById('threeStockManagementItem');
    stockManagementItemElement.selectedIndex = material.stockManagementItem ? 1 : 2;

    document.getElementById('threeCompanyName').value = material.companyName;
    document.getElementById('threeCompanyCode').value = material.companyCode;
}

// 수동으로 폼 리셋 함수 (저장 후 폼 초기화)
function resetMaterialForm() {
    document.getElementById('threeMaterialCode').value = '';  // materialCode 필드를 비움
    document.getElementById('threeMaterialCode').readOnly = false;  // materialCode 수정 가능하게 변경
    document.getElementById('threeMaterialName').value = '';  // 재료명 필드를 비움
    document.getElementById('materialUnit').value = '';  // 단위 필드를 비움
    document.getElementById('materialUnitPrice').value = '';  // 단가 필드를 비움
    document.getElementById('minQuantity').value = '';  // 최소 수량 필드를 비움
    document.getElementById('threeStockManagementItem').selectedIndex = 0;  // 재고관리 여부 초기화
    document.getElementById('threeCompanyName').value = '';  // 업체명 필드를 비움
    document.getElementById('threeCompanyCode').value = '';  // 업체코드 필드를 비움
}

// 재료 추가 또는 수정 버튼 클릭 이벤트 핸들러
document.getElementById('addMaterialBtn').addEventListener('click', (event) => {
    event.preventDefault();  // 기본 폼 제출 동작을 막음
    event.stopPropagation();  // 이벤트 전파 방지

    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const materialCode = document.getElementById('threeMaterialCode').value;

    // POST 또는 PUT 요청을 보낼지 결정
    let url, method;
    if (materialCode) {
        url = `/inventory_management/updateMaterial`;  // 수정 API
        method = "PUT";  // PUT 메서드 사용
    } else {
        url = `/inventory_management/addMaterial`;  // 저장 API
        method = "POST";  // POST 메서드 사용
    }

    const materialData = {
        materialCode: materialCode,
        materialName: document.getElementById('threeMaterialName').value,
        materialUnit: document.getElementById('materialUnit').value,
        materialUnitPrice: parseFloat(document.getElementById('materialUnitPrice').value),
        minQuantity: parseInt(document.getElementById('minQuantity').value, 10),
        stockManagementItem: document.getElementById('threeStockManagementItem').value === 'y',
        companyName: document.getElementById('threeCompanyName').value,
        companyCode: document.getElementById('threeCompanyCode').value
    };
    // 서버로 데이터 전송
    fetch(url, {
        method: method,
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
            resetMaterialForm();  // 폼 리셋
            loadMaterialList();  // 목록 다시 로딩
        })
        .catch(error => {
            console.error("Error:", error);
            alert(`서버와의 통신 중 오류가 발생했습니다. 상세 내용: ${error.message}`);
        });

});


// 재료 목록 로딩 함수
function loadMaterialList() {
    fetch('/inventory_management/searchMaterial?materialName=')  // 검색어를 빈 문자열로 전달해서 전체 목록 조회
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('materialCompanyList');
            tbody.innerHTML = '';  // 기존 행 제거

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 재료가 없습니다.</td></tr>';
            } else {
                data.forEach(material => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${material.companyName}</td>
                        <td>${material.companyCode}</td> 
                        <td>${material.materialName}</td>
                        <td>${material.materialCode}</td>
                        <td>${material.materialUnit}</td>
                        <td>${material.materialUnitPrice}</td>
                        <td>${material.minQuantity}</td>
                        <td>${material.stockManagementItem ? "예" : "아니오"}</td>
                    `;
                    row.addEventListener('dblclick', () => {
                        populateMaterialForm(material);
                        // 기존 선택된 행에서 하이라이트 제거
                        if (selectedRow) {
                            selectedRow.classList.remove('selected-highlight');
                        }

                        // 현재 선택된 행에 하이라이트 추가
                        row.classList.add('selected-highlight');
                        selectedRow = row;  // 선택된 행 업데이트
                    });

                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 목록을 불러오는 중 오류 발생:", error);
        });
}

// 재료 검색 함수
function threeSearch() {
    const companyName = document.getElementById('threeCompanyNameSearch').value;
    const materialName = document.getElementById('threeMaterialNameSearch').value;

    let url = `/inventory_management/searchMaterial?`;
    if (companyName) url += `companyName=${encodeURIComponent(companyName)}&`;
    if (materialName) url += `materialName=${encodeURIComponent(materialName)}`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('materialCompanyList');
            tbody.innerHTML = ''; // 기존 행 제거

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7">현재 등록된 재료가 없습니다.</td></tr>';
            } else {
                data.forEach(material => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${material.companyName}</td>
                        <td>${material.companyCode}</td>
                        <td>${material.materialName}</td>
                        <td>${material.materialCode}</td>
                        <td>${material.materialUnit}</td>
                        <td>${material.materialUnitPrice}</td>
                        <td>${material.minQuantity}</td>
                        <td>${material.stockManagementItem ? "예" : "아니오"}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("검색 중 오류 발생:", error);
        });
}

// 초기화 버튼 클릭 시 호출되는 함수
function resetSearch() {
    // 검색어 초기화
    document.getElementById('threeCompanyNameSearch').value = '';
    document.getElementById('threeMaterialNameSearch').value = '';

    // 전체 데이터를 불러오는 요청
    fetch('/inventory_management/searchMaterial')
        .then(response => response.json())
        .then(data => {
            const tbody = document.getElementById('materialCompanyList');
            tbody.innerHTML = '';  // 기존 행 제거

            if (data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="8">현재 등록된 재료가 없습니다.</td></tr>';
            } else {
                data.forEach(material => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${material.companyName}</td>
                        <td>${material.companyCode}</td>
                        <td>${material.materialName}</td>
                        <td>${material.materialCode}</td>
                        <td>${material.materialUnit}</td>
                        <td>${material.materialUnitPrice}</td>
                        <td>${material.minQuantity}</td>
                        <td>${material.stockManagementItem ? "예" : "아니오"}</td>
                    `;
                    tbody.appendChild(row);
                });
            }
        })
        .catch(error => {
            console.error("재료 목록을 불러오는 중 오류 발생:", error);
        });
}

// 페이지 로드 시 재료 목록을 불러오는 함수 호출
loadMaterialList();  // 초기 데이터 로딩


// 업체 목록을 더블클릭했을 때 선택된 업체 정보를 input 필드에 채우는 함수
function selectCompany(companyCode, companyName) {
    // 선택한 업체 정보를 input 필드에 채우기
    document.getElementById('threeCompanyCode').value = companyCode;
    document.getElementById('threeCompanyName').value = companyName;

    // 모달 닫기
    const modal = bootstrap.Modal.getInstance(document.getElementById('companyModal'));
    modal.hide();
}

// 서버에서 업체 목록을 불러오는 함수
async function fetchCompanies() {
    try {
        const response = await fetch('/inventory_management/searchCompanies'); // 서버에서 업체 목록을 가져오는 API 호출
        const companies = await response.json();

        const companyList = document.getElementById('companyList');
        companyList.innerHTML = ''; // 기존 목록 초기화

        // 서버에서 받은 업체 데이터를 테이블에 추가
        companies.forEach(company => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${company.companyCode}</td>
                <td>${company.companyName}</td>
                <td>${company.companyNumber}</td>
                <td>${company.managerName}</td>
                <td>${company.managerNumber}</td>
            `;

            // 행을 더블클릭했을 때 업체 선택 함수 호출
            row.addEventListener('dblclick', function() {
                selectCompany(company.companyCode, company.companyName);
            });

            companyList.appendChild(row);
        });
    } catch (error) {
        console.error("재료 목록을 불러오는 중 오류 발생:", error);
    }
}

// 모달이 열릴 때마다 업체 목록을 불러오기
document.getElementById('companyModal').addEventListener('show.bs.modal', fetchCompanies);

// 돋보기 버튼 클릭 시 모달을 띄우는 함수
document.getElementById('companySearchIcon').addEventListener('click', function() {
    const companyModal = new bootstrap.Modal(document.getElementById('companyModal'));
    companyModal.show(); // 모달을 띄우기
});



// 재료 삭제 버튼 클릭 시 데이터 전송
document.getElementById('deleteMaterialBtn').addEventListener('click', function() {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const materialCode = document.getElementById('threeMaterialCode').value;

    if (confirm("정말로 이 재료를 삭제하시겠습니까?")) {
        fetch(`/inventory_management/deleteMaterial?materialCode=${encodeURIComponent(materialCode)}`, {
            method: "DELETE"
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.text();
            })
            .then(data => {
                alert(data);  // 서버로부터 삭제 성공 메시지 받음
                // 폼 초기화 및 목록 새로고침
                document.getElementById('materialForm').reset();
                loadMaterialList();
            })
            .catch(error => {
                console.error("삭제 요청 중 오류 발생:", error);
                alert("재료 삭제에 실패했습니다. 다시 시도해주세요.");
            });
    }
});

// **초기화 버튼 클릭 이벤트 핸들러 추가**
document.getElementById('threeSearchReset').addEventListener('click', function() {
    resetSearch();  // 검색 필드 초기화 및 전체 목록 로드
});
