
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


document.addEventListener("DOMContentLoaded", function () {
    const socket = new SockJS('/patientCount');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        console.log('WebSocket connected:', frame);

        // 환자 수 업데이트 메시지 구독
        stompClient.subscribe('/patientCount', function (message) {
            const data = JSON.parse(message.body);
            const {status, count} = data;

            // 상태 및 카운트를 출력하여 확인
            console.log(`Status: ${status}, Count: ${count}`); // 수정된 부분

            // UI 업데이트
            switch(status) {
                case "1":
                    document.getElementById('home-waitingCount').textContent = count;
                    break;
                case "2":
                    document.getElementById('home-inTreatmentCount').textContent = count;
                    break;
                case "3":
                    document.getElementById('home-completedCount').textContent = count;
                    break;
                default:
                    console.warn(`Unknown status: ${status}`); // 알 수 없는 상태에 대한 경고
            }
        });
    }, function(error) {
        console.error('WebSocket connection error:', error); // 연결 실패 시
    });
});

// 페이지 로드 시 호출
function goToReception() {
    window.location.href = "http://localhost:8080/reception";
}






