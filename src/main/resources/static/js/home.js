
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