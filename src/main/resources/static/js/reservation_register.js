renderCalendar();

// 드롭다운 메뉴 초기화
reservationTimes.forEach((time, index) => {
    const test_time = document.getElementById('test_time');
    const option = document.createElement('option');
    option.value = time;
    option.textContent = time;

    // 첫 번째 옵션을 기본값으로 설정
    if (index === 0) {
        option.selected = true;
    }

    test_time.appendChild(option);
});

function dateReservationList(selectedDate) {
    console.log(selectedDate);
    fetch('reservation/selectedDatePatientList', {

        method: 'POST', // POST 요청
        headers: {
            'Content-Type': 'application/json' // JSON 형식으로 데이터 전송
        },
        body: JSON.stringify({
            reservationDate: selectedDate
        }) // JSON 객체로 전송
    })
        .then(response => {
            // 응답 상태가 성공적인 경우 JSON으로 변환
            if (!response.ok) {
                throw new Error('네트워크 응답이 실패했습니다.');
            }
            return response.json(); // JSON 데이터로 변환
        })
        .then(data => {
            // 환자 데이터가 들어갈 ID값 보관
            const tableBody = document.querySelector('#reservationTableList');

            // 테이블의 기존 데이터를 지우고 새 데이터를 추가
            // 해당 작업은 다른 날짜를 클릭했을때 기존 내용을 지워야 하기 때문임
            tableBody.innerHTML = ''; // 기존 내용 제거

            // 데이터 배열을 순회하여 테이블에 추가
            data.forEach(item => {

                // 시간만 추출 (예: "2024-10-21T00:13" -> "00:13")
                const time = new Date(item.reservationDate).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                });


                const row = document.createElement('tr'); // 새로운 행 생성
                row.id = 'reservationTableListParent'; // ID 추가
                row.innerHTML = `
                                <td>${time}</td>
                                <td>${item.department}</td>
                                <td>${item.patientNote}</td>
                                `; // 각 열에 데이터 삽입

                row.onclick = function () {
                    selectList(item.seq);
                };

                tableBody.appendChild(row); // tbody에 행 추가
            });

        })
        .catch(error => {
            // 에러 처리
            console.error('에러 발생:', error);
        });
}


// 예약 목록에서 환자 정보를 눌렀을때 데이터 받아온 뒤 수정 가능한 화면 만들기
function selectList(indexNumber) {

    // 보낼 데이터 객체로 변환
    const data = {
        seq: indexNumber
    };

    // 서버로 객체 전송
    fetch('reservation/selectedByReservation', {
        method: 'POST', // POST 요청
        headers: {
            'Content-Type': 'application/json' // JSON 형식으로 데이터 전송
        },
        body: JSON.stringify(data) // JSON 객체로 전송
    })
        .then(response => response.json()) // 응답을 JSON으로 변환
        .then(responseData => {
            rReset();
            rReset2(responseData);
        })
        .catch(error => {
            console.error('에러 발생:', error); // 에러 처리
        });
}

function rReset(reset) {

    if (reset) {
        const IndexElement = document.getElementById('index-number');
        IndexElement.innerHTML = '';
    }

    const reservationDateElement = document.getElementById('reservation-date');
    reservationDateElement.value = '';

    const reservationTimeElement = document.getElementById('test_time');
    reservationTimeElement.value = "9:00";

    // 환자이름
    const departmentElement = document.getElementById('departmentInput');
    departmentElement.value = '';

    // 예약 종류 - 환자 예약
    const snsNotificationElement = document.getElementById('sns-notification');
    snsNotificationElement.checked = false;

    // 차트번호
    const chartNumberElement = document.getElementById('chart-numberInput');
    chartNumberElement.value = '';

    const doctorElement = document.getElementById('doctor');
    doctorElement.selectedIndex = 0;

    const treatmentTypeElement = document.getElementById('treatment-type');
    treatmentTypeElement.selectedIndex = 0;

    // 노트
    const patientNoteElement = document.getElementById('patient-note');
    patientNoteElement.value = '';

    // 예약 미이행 - c/A
    const reservationStatusCaElement = document.getElementById('reservation-status-ca');
    reservationStatusCaElement.checked = false;

    // 예약 미이행 - B/A
    const reservationStatusBaElement = document.getElementById('reservation-status-ba');
    reservationStatusBaElement.checked = false;

    // 예약 미이행 없음
    const reservationStatusElement = document.getElementById('reservation-status-none');
    reservationStatusElement.checked = false;

    // 인덱스
    const indexNumberElement = document.getElementById('index-number');
    indexNumberElement.value = '';

}

function rReset2(responseData) {
    const reservationDateElement = document.getElementById('reservation-date');
    const reservationTimeElement = document.getElementById('test_time'); // 예약시간 요소 추가
    const departmentElement = document.getElementById('departmentInput');
    const snsNotificationElement = document.getElementById('sns-notification');
    const chartNumberElement = document.getElementById('chart-numberInput');
    const doctorElement = document.getElementById('doctor');
    const treatmentTypeElement = document.getElementById('treatment-type');
    const patientNoteElement = document.getElementById('patient-note');
    const reservationStatusCaElement = document.getElementById('reservation-status-ca');
    const reservationStatusBaElement = document.getElementById('reservation-status-ba');
    const reservationStatusElement = document.getElementById('reservation-status-none');
    const indexNumberElement = document.getElementById('index-number');

    // 받아온 데이터로 데이터 새로 등록

    if (responseData.length > 0) {
        // 예약 날짜와 시간 분리하여 설정
        const combinedDateTime = responseData[0].reservationDate.split('T'); // 공백으로 분리
        reservationDateElement.value = combinedDateTime[0]; // 날짜
        reservationTimeElement.value = combinedDateTime[1]; // 시간

        // 환자 이름
        departmentElement.value = responseData[0].department;

        // SMS 발송
        snsNotificationElement.checked = responseData[0].snsNotification === "true";

        // 차트 번호
        chartNumberElement.value = responseData[0].chartNumber;

        // 의사 값 설정
        doctorElement.value = responseData[0].doctor;

        // 치료 유형 값 설정
        treatmentTypeElement.value = responseData[0].treatmentType;

        // 노트
        patientNoteElement.value = responseData[0].patientNote;

        // 예약 미이행 체크 설정
        if (responseData[0].reservationStatusCheck === "ca") {
            reservationStatusCaElement.checked = true;
        } else if (responseData[0].reservationStatusCheck === "ba") {
            reservationStatusBaElement.checked = true;
        } else if (responseData[0].reservationStatusCheck === "없음") {
            reservationStatusElement.checked = true;
        }

        // 인덱스 번호
        indexNumberElement.innerHTML = responseData[0].seq;
    }
}


function saveUpdate() {
    // 권한 체크
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    // 예약일자 및 예약시간
    const reservationDate = document.getElementById('reservation-date').value;
    const reservationTime = document.getElementById('test_time').value; // 예약시간 추가
    const timeParts = reservationTime.split(':');
    const hour = timeParts[0].padStart(2, '0');
    const minute = timeParts[1] || '00';

    const formattedDateTime = `${reservationDate}T${hour}:${minute}`;

    // 환자 이름
    const department = document.getElementById('departmentInput').value;

    // SMS 발송 허용 여부
    const snsNotification = document.getElementById('sns-notification').checked ? "true" : "false";

    // 차트 번호
    const chartNumber = document.getElementById('chart-numberInput').value;

    // 의사
    const doctor = document.getElementById('doctor').value;

    // 치료 유형
    const treatmentType = document.getElementById('treatment-type').value;

    // 환자 노트
    const patientNote = document.getElementById('patient-note').value;

    // 예약 미이행
    let reservationStatusCheck = "";
    if (document.getElementById('reservation-status-ca').checked) {
        reservationStatusCheck = "ca";
    } else if (document.getElementById('reservation-status-ba').checked) {
        reservationStatusCheck = "ba";
    } else if (document.getElementById('reservation-status-none').checked) {
        reservationStatusCheck = "없음";
    }

    const indexNumber = document.getElementById('index-number').innerHTML.trim();

    // 보낼 데이터 객체로 변환
    const reservation_data = {
        reservationDate: formattedDateTime, // 변경된 부분
        department: department,
        snsNotification: snsNotification,
        chartNumber: chartNumber,
        doctor: doctor,
        treatmentType: treatmentType,
        patientNote: patientNote,
        reservationStatusCheck: reservationStatusCheck
    };

    fetch('reservation/selectedReservation', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(reservation_data)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('중복 확인 요청 실패');
            }
            return response.json();
        })
        .then(reservations => {
            console.log(reservations.length);
            if (reservations.length === 0) {
                console.log(JSON.stringify(reservation_data));
                fetch('reservation/insertReservationInformation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(reservation_data)
                })
                    .then(response => {
                        if (!response.ok) {
                            throw new Error('Network response was not ok');
                        }
                        return response.json();
                    })
                    .then(reservations => {
                        const indexNumberElement = document.getElementById('index-number');
                        reservations.forEach(res => {
                            console.log(res.seq);
                            const dateOnly = new Date(formattedDateTime).toLocaleDateString('en-CA');
                            dateReservationList(dateOnly);
                            indexNumberElement.innerHTML = res.seq;
                        });
                    })
                    .catch(error => {
                        console.error('실패:', error);
                    });
            } else {
                if (indexNumber) {
                    reservation_data.seq = indexNumber;
                    fetch('reservation/updateReservationInformation', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(reservation_data)
                    })
                        .then(response => {
                            const dateOnly = new Date(formattedDateTime).toLocaleDateString('en-CA');
                            dateReservationList(dateOnly);
                            console.log("!!!!!!!!!!" + indexNumber);
                        })
                        .catch(error => {
                            console.error('실패:', error);
                        });
                }
            }
        })
        .catch(error => {
            console.error('오류 발생:', error);
        });
}


function deleteReservation() {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const indexNumber = document.getElementById('index-number').innerHTML.trim();
    const reservationDate = document.getElementById('reservation-date').value;

    if (!indexNumber) {

        return;
    }

    const confirmation = confirm("정말로 삭제하시겠습니까?");
    if (!confirmation) return;

    // 보낼 데이터 객체로 변환
    const data = {
        seq: indexNumber
    };

    // 서버에 삭제 요청 보내기
    fetch(`reservation/deleteReservation?seq=${data.seq}`, { // seq를 URL에 포함
        method: 'DELETE', // DELETE 요청
        headers: {
            'Content-Type': 'application/json' // JSON 형식으로 데이터 전송
        }
    })
        .then(data => {
            alert("예약이 성공적으로 삭제되었습니다.");
            // 삭제 후 UI 갱신 등 추가 로직
            rReset();
            const dateOnly = new Date(reservationDate).toLocaleDateString('en-CA');
            dateReservationList(dateOnly);


        })
        .catch(error => {
            console.error('에러 발생:', error);
            alert("삭제 중 오류가 발생했습니다.");
        });

}
