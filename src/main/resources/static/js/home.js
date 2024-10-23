
// 최소보관수량 미달 품목 리스트 로딩
$.ajax({
    type: 'GET',
    url: '/home/low-stock-items',
    success: function (data) {
        const lowStockList = $('#lowStockList');
        lowStockList.empty(); // 기존 내용을 지움

        if (data.length === 0) {
            lowStockList.append('<tr><td colspan="3">최소보관수량 미달품목이 없습니다.</td></tr>');
        } else {
            data.forEach(item => {
                const row = `
                        <tr>
                            <td>${item.materialName}</td>
                            <td>${item.materialCode}</td>
                            <td>${item.remainingStock}</td>
                        </tr>
                    `;
                lowStockList.append(row);
            });
        }
    },
    error: function (xhr, status, error) {
        console.error('Error fetching low stock items:', error);
    }
});


function goToMaterialManagementPage() {
    window.location.href = '/inventory_management';
}




document.addEventListener('DOMContentLoaded', function() {
    console.log('스크립트가 로드되었습니다.');
    console.log('DOM이 완전히 로드되고 파싱되었습니다.');

    function getCurrentDate() {
        const today = new Date();
        const yyyy = today.getFullYear();
        const mm = String(today.getMonth() + 1).padStart(2, '0');
        const dd = String(today.getDate()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}`;
    }

    function fetchTreatmentCount(status) {
        const currentDate = getCurrentDate();
        const url = `/api/patient-admission/completeTreatment/${status}/${currentDate}`;

        console.log(`데이터를 가져오는 URL: ${url}`);

        fetch(url)
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => {
                        console.error('오류 세부 정보:', err);
                        throw new Error(err.message || '진료 시작 실패');
                    });
                }
                return response.json();
            })
            .then(count => {
                console.log(`상태 ${status}에 ${count}명의 환자가 있습니다.`);

                if (status === 1) {
                    document.getElementById('home-waitingCount').textContent = count;
                } else if (status === 2) {
                    document.getElementById('home-inTreatmentCount').textContent = count;
                } else if (status === 3) {
                    document.getElementById('home-completedCount').textContent = count;
                }
            })
            .catch(error => console.error('카운트 가져오기 오류:', error));
    }

    function updatePatientCounts() {
        console.log('환자 수를 가져오는 중...');
        fetchTreatmentCount(1);
        fetchTreatmentCount(2);
        fetchTreatmentCount(3);
    }

    document.getElementById('updateButton').addEventListener('click', updatePatientCounts);
    updatePatientCounts();
});

