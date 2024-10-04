document.addEventListener('DOMContentLoaded', function () {

    // -------------------- 재료 출납 관리 관련 기능 --------------------

    // 트랜잭션 저장 버튼 클릭 이벤트
    document.getElementById('saveTransaction').addEventListener('click', (event) => {
        event.preventDefault();  // 기본 폼 제출 방지

        const transactionDate = document.getElementById('transactionDate').value;
        let url, method;

        // 트랜잭션 저장 또는 수정 여부에 따른 URL 및 메소드 설정
        if (transactionDate) {
            url = `/transaction_management/updateTransaction`;
            method = "PUT";
        } else {
            url = `/transaction_management/addTransaction`;
            method = "POST";
        }

        const transactionData = {
            transactionDate: document.getElementById('transactionDate').value,
            companyName: document.getElementById('twoCompanyName').value,
            materialName: document.getElementById('twoMaterialName').value,
            materialCode: document.getElementById('twoMaterialCode').value,
            stockIn: parseInt(document.getElementById('stockIn').value, 10),
            stockOut: parseInt(document.getElementById('stockOut').value, 10),
            managerNumber: document.getElementById('managerNumber').value
        };

        // 서버에 데이터 전송
        fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(transactionData)
        })
            .then(response => response.json())
            .then(data => {
                alert(data.message);  // 서버 메시지 알림
                loadTransactionList();  // 트랜잭션 목록 다시 로딩
            })
            .catch(error => {
                alert(`오류 발생: ${error.message}`);
            });
    });

    // 트랜잭션 목록 불러오기
    function loadTransactionList() {
        fetch('/transaction_management/searchTransaction')
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('transactionList');
                tbody.innerHTML = '';  // 기존 행 초기화

                if (data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="7">현재 등록된 출납 내역이 없습니다.</td></tr>';
                } else {
                    data.forEach(transaction => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${transaction.transactionDate}</td>
                            <td>${transaction.companyName}</td>
                            <td>${transaction.materialName}</td>
                            <td>${transaction.materialCode}</td>
                            <td>${transaction.stockIn}</td>
                            <td>${transaction.stockOut}</td>
                            <td>${transaction.managerNumber}</td>
                        `;
                        row.addEventListener('dblclick', () => populateTransactionForm(transaction));
                        tbody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error("출납 내역을 불러오는 중 오류 발생:", error);
            });
    }

    // 트랜잭션 삭제 버튼 클릭 이벤트
    document.getElementById('deleteTransactionBtn').addEventListener('click', () => {
        const transactionDate = document.getElementById('transactionDate').value;

        if (confirm("이 출납 내역을 삭제하시겠습니까?")) {
            fetch(`/transaction_management/deleteTransaction?transactionDate=${encodeURIComponent(transactionDate)}`, {
                method: "DELETE"
            })
                .then(response => response.json())
                .then(data => {
                    alert(data.message);
                    resetTransactionForm();
                    loadTransactionList();
                })
                .catch(error => {
                    alert(`삭제 중 오류 발생: ${error.message}`);
                });
        }
    });

    // 재료 조회 버튼 클릭 시 모달창을 띄우고 재료 목록을 조회하는 함수
    document.getElementById('materialCompanySelect').addEventListener('click', function () {
        console.log("재료 조회 버튼 클릭됨");
        loadMaterialCompanyList();  // 모달 열기 전에 데이터 로드
        const materialCompanyModal = new bootstrap.Modal(document.getElementById('materialCompanyModal'));
        materialCompanyModal.show();  // 모달 열기
    });

    function loadMaterialCompanyList() {
        console.log("재료/업체 목록 불러오기 시작");
        // 서버에서 재료/업체 목록 불러오기
        fetch('/material_register/getMaterialCompanyList')  // 서버 API 호출
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('materialCompanyList');
                tbody.innerHTML = '';  // 기존 내용 초기화

                if (data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="3">등록된 업체나 재료가 없습니다.</td></tr>';
                } else {
                    data.forEach(material => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${material.companyName}</td>
                            <td>${material.materialName}</td>
                            <td>${material.materialCode}</td>
                        `;
                        row.addEventListener('click', function () {
                            selectMaterial(material);
                        });
                        tbody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error("업체/재료 목록을 불러오는 중 오류 발생:", error);
            });
    }

    // 선택한 업체/재료 정보를 입력 필드에 채우기
    function selectMaterial(material) {
        document.getElementById('twoCompanyCode').value = material.companyCode;
        document.getElementById('twoMaterialName').value = material.materialName;
        document.getElementById('twoMaterialCode').value = material.materialCode;

        // 모달 닫기
        const modal = bootstrap.Modal.getInstance(document.getElementById('materialCompanyModal'));
        modal.hide();
    }


    // 선택한 업체/재료 정보를 입력 필드에 채우기
    function selectMaterial(material) {
        document.getElementById('twoCompanyCode').value = material.companyCode;
        document.getElementById('twoCompanyName').value = material.companyName;
        document.getElementById('twoMaterialName').value = material.materialName;
        document.getElementById('twoMaterialCode').value = material.materialCode;

        // 모달 닫기
        const modal = bootstrap.Modal.getInstance(document.getElementById('materialCompanyModal'));
        modal.hide();
    }

    // 재료 및 업체 검색 함수
    function twoSearch() {
        const materialName = document.getElementById('twoMaterialNameSearch').value;
        const materialCode = document.getElementById('twoMaterialCodeSearch').value;

        let url = `/transaction_management/searchTransaction?`;
        if (materialName) url += `materialName=${encodeURIComponent(materialName)}&`;
        if (materialCode) url += `materialCode=${encodeURIComponent(materialCode)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const tbody = document.getElementById('transactionList');
                tbody.innerHTML = '';  // 기존 행 초기화

                if (data.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="7">등록된 출납 내역이 없습니다.</td></tr>';
                } else {
                    data.forEach(transaction => {
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td>${transaction.transactionDate}</td>
                            <td>${transaction.companyName}</td>
                            <td>${transaction.materialName}</td>
                            <td>${transaction.materialCode}</td>
                            <td>${transaction.stockIn}</td>
                            <td>${transaction.stockOut}</td>
                            <td>${transaction.managerNumber}</td>
                        `;
                        tbody.appendChild(row);
                    });
                }
            })
            .catch(error => {
                console.error("검색 중 오류 발생:", error);
            });
    }

    // 트랜잭션 폼 초기화 함수
    function resetTransactionForm() {
        document.getElementById('transactionDate').value = '';
        document.getElementById('twoCompanyCode').value = '';
        document.getElementById('twoMaterialName').value = '';
        document.getElementById('twoMaterialCode').value = '';
        document.getElementById('stockIn').value = '';
        document.getElementById('stockOut').value = '';
    }
});