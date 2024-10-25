function dateReservationList(selectedDate) {
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

                //
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

function renderCalendar() {
    const currentDate = new Date();
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const lastDate = new Date(year, month + 1, 0).getDate();
    const monthYear = document.getElementById('monthYear');
    const calendarBody = document.getElementById('calendar-body');
    monthYear.innerText = `${year}년 ${month + 1}월`;
    calendarBody.innerHTML = '';

    let date = 1;
    for (let i = 0; i < 6; i++) {
        const row = document.createElement('tr');

        for (let j = 0; j < 7; j++) {
            const cell = document.createElement('td');
            cell.classList.add('calendarDate');
            if (i === 0 && j < firstDay) {
                cell.innerHTML = '';
            } else if (date > lastDate) {
                break;
            } else {
                cell.innerHTML = date;
                date++;
            }
            row.appendChild(cell);
        }

        calendarBody.appendChild(row);
    }


    // 캘린더 날짜 선택시 오늘 날짜 정보 받아오기
    document.querySelectorAll('.calendarDate').forEach(function (calendarDate) {
        calendarDate.addEventListener('click', function () {
            // 클릭된 날짜의 텍스트 가져오기
            const day = this.textContent;

            // 저장한 날짜 텍스트, 날짜 형식으로 재변환
            const selectedDate = `${year}-${month + 1}-${day}`;
            console.log(selectedDate)

            dateReservationList(selectedDate);
        });
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

    // 예약 날짜
    if (reservationDateElement && responseData.length > 0) {
        reservationDateElement.value = responseData[0].reservationDate;
    }
    // 환자 이름
    if (departmentElement && responseData.length > 0) {
        departmentElement.value = responseData[0].department;
    }

    // SMS 발송
    if (snsNotificationElement && responseData.length > 0) {
        if (responseData[0].snsNotification) {
            snsNotificationElement.checked = true;
        } else {
            snsNotificationElement.checked = false;
        }
    }

    // 차트 번호
    if (chartNumberElement && responseData.length > 0) {
        chartNumberElement.value = responseData[0].chartNumber;
    }

    if (doctorElement && responseData.length > 0) {
        doctorElement.value = responseData[0].doctor; // 의사 값 설정
    }

    if (treatmentTypeElement && responseData.length > 0) {
        treatmentTypeElement.value = responseData[0].treatmentType; // 치료 유형 값 설정
    }

    // 노트
    if (patientNoteElement && responseData.length > 0) {
        patientNoteElement.value = responseData[0].patientNote;
    }

    // 예약 미이행
    if (reservationStatusCaElement && reservationStatusBaElement && reservationStatusElement && responseData.length > 0) {
        if (responseData[0].reservationStatusCheck === "ca") {
            reservationStatusCaElement.checked = true;
        } else if (responseData[0].reservationStatusCheck === "ba") {
            reservationStatusBaElement.checked = true;
        } else if (responseData[0].reservationStatusCheck === "없음") {
            reservationStatusElement.checked = true;
        }
    }

    // 인덱스 번호
    if (indexNumberElement && responseData.length > 0) {
        indexNumberElement.innerHTML = responseData[0].seq;
    }
}


// 저장 버튼 눌렀을 때 업데이트 혹은 저장이 이루어지게 하기
function saveUpdate() {
// 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }
    // 에약 일자
    let reservationDate = document.getElementById('reservation-date').value;

    // 환자 이름
    let department = document.getElementById('departmentInput').value;

    // SMS 발송 허용 여부
    let snsNotification = "";
    if (document.getElementById('sns-notification').checked) {
        snsNotification = "true";
    } else {
        snsNotification = "false";
    }

    // 차트 번호
    let chartNumber = document.getElementById('chart-numberInput').value;

    // 의사
    let doctor = document.getElementById('doctor').value;

    // 치료 유형
    let treatmentType = document.getElementById('treatment-type').value;

    // 환자 노트
    let patientNote = document.getElementById('patient-note').value;

    // 예약 미이행
    let reservationStatusCheck = "";

    if (document.getElementById('reservation-status-ca').checked) {
        reservationStatusCheck = "ca";
    } else if (document.getElementById('reservation-status-ba').checked) {
        reservationStatusCheck = "ba";
    } else if (document.getElementById('reservation-status-none').checked) { // 'xyz' 항목 추가
        reservationStatusCheck = "없음";
    }

    let indexNumber = document.getElementById('index-number').innerHTML.trim();

    // 보낼 데이터 객체로 변환
    const reservation_data = {
        reservationDate: reservationDate,
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
    }).then(response => {
        if (!response.ok) {
            throw new Error('중복 확인 요청 실패');
        }
        return response.json(); // JSON 데이터로 변환
    })
        .then(reservations => {
            // chartNum, 예약일자 중복 안되면 실행
            console.log(reservations.length)
            if (reservations.length === 0) {
                console.log(JSON.stringify(reservation_data))
                // 서버에 데이터 값 보내주기
                fetch('reservation/insertReservationInformation', {
                    method: 'POST', // POST 요청
                    headers: {
                        'Content-Type': 'application/json' // JSON 형식으로 데이터 전송
                    },
                    body: JSON.stringify(reservation_data) // JSON 객체로 전송
                }).then(response => {
                    // 응답을 JSON으로 변환
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                }).then(reservations => {
                    const indexNumberElement = document.getElementById('index-number');
                    reservations.forEach(res => {
                        console.log(res.seq)
                        console.log(res)
                        const dateOnly = new Date(reservationDate).toLocaleDateString('en-CA');
                        console.log(dateOnly);
                        dateReservationList(dateOnly);
                        indexNumberElement.innerHTML = res.seq;
                    })
                    // 날짜만 가져오기

                })
                    .catch(error => {
                        console.error('실패:', error);
                    });
            } else {
                // chartNum, 예약일자 중복이면 실행
                // 인덱스 번호가 있는 경우 수정
                if (indexNumber) {
                    // 인덱스 번호가 있기 때문에 객체에 seq(인덱스 번호) 값 추가
                    reservation_data.seq = indexNumber;

                    fetch('reservation/updateReservationInformation', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(reservation_data)
                    })
                        .then(response => {

                            // 날짜만 가져오기
                            const dateOnly = new Date(reservationDate).toLocaleDateString('en-CA');
                            dateReservationList(dateOnly);
                            console.log("!!!!!!!!!!" + indexNumber)
                        })
                        .catch(error => {
                            console.error('실패:', error);
                        });
                }
                // 중복이 있을 경우의 처리
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


function prevMonth() {
    currentDate.setMonth(currentDate.getMonth() - 1);
    renderCalendar();
}

function nextMonth() {
    currentDate.setMonth(currentDate.getMonth() + 1);
    renderCalendar();
}

renderCalendar();