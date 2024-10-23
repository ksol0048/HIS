
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




async function fetchInitialCounts() {
    const today = new Date().toISOString().split('T')[0]; // 오늘 날짜를 yyyy-MM-dd 형식으로
    const statuses = [1, 2, 3]; // 요청할 상태 배열

    try {
        for (const status of statuses) {
            const response = await fetch(`/completeTreatment/${status}/${today}`); // API 호출

            if (!response.ok) {
                throw new Error(`환자 수 가져오기 실패: ${response.statusText}`);
            }

            const count = await response.json(); // 응답을 JSON으로 변환
            console.log(`상태 ${status}에 ${count}명의 환자가 있습니다.`); // 콘솔에 출력

            // 각각의 상태에 따른 카운트 업데이트
            if (status === 1) {
                document.getElementById('home-waitingCount').textContent = count;
            } else if (status === 2) {
                document.getElementById('home-inTreatmentCount').textContent = count;
            } else if (status === 3) {
                document.getElementById('home-completedCount').textContent = count;
            }
        }
    } catch (error) {
        console.error('에러 발생:', error); // 에러가 발생하면 콘솔에 출력
    }
}

// 페이지 로드 시 호출
document.addEventListener('DOMContentLoaded', fetchInitialCounts);






