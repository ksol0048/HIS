document.addEventListener("DOMContentLoaded", function () {
    const waitingPatientsTable = document.getElementById("waitingPatientsTable").getElementsByTagName("tbody")[0];
    const treatmentPatientsTable = document.getElementById("treatmentPatientsTable").getElementsByTagName("tbody")[0];
    const completePatientsTable = document.getElementById("completedPatientsTable").getElementsByTagName("tbody")[0];
    let waitingPatientCount = 0;
    let treatmentPatientCount = 0;
    let completePatientCount = 0;


    // 오늘 날짜 기본값 설정
    const today = new Date();
    document.getElementById('currentDate').value = today.toISOString().substring(0, 10);


    // 접수 버튼 클릭 이벤트 추가
    const receptionBtn = document.querySelector(".ReceptionBtn");
    receptionBtn.addEventListener("click", function () {
        console.log("접수 버튼이 클릭되었습니다.");

        // 세션에서 환자 데이터 가져오기
        const selectedPatient = JSON.parse(sessionStorage.getItem('selectedPatient'));

        if (selectedPatient) {
            console.log("가져온 환자 정보:", selectedPatient);

            // 환자 데이터 객체 생성
            const patientData = {
                chartNum: selectedPatient.chartNum, // 차트 번호
                paName: selectedPatient.name, // 환자 이름
                mainDoc: null,  // 의사는 null로 설정
                rvTime: selectedPatient.rvTime, // 환자의 방문 시간
                receptionTime: new Date().toISOString() // 현재 날짜 및 시간
            };

            // 새로 등록하기
            fetch("/api/patient-admission/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(patientData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || '환자 접수 실패');
                        });
                    }
                    return response.json(); // JSON으로 응답을 파싱
                })
                .then(data => {
                    console.log(data);
                    alert("환자 접수가 완료되었습니다."); // 접수 완료 알림 추가

                    // 대기 중 테이블에 추가
                    addPatientToWaitingTable({
                        chartNum: patientData.chartNum, // 차트 번호
                        paName: patientData.paName, // 환자 이름
                        treatStatus: "1", // 대기 상태
                        receptionTime: patientData.receptionTime // 접수 시간
                    });
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert(error.message);
                });
        } else {
            console.log("세션에서 환자 정보가 없습니다.");
            alert("선택된 환자가 없습니다.");
        }
    });




    const startTreatmentButton = document.getElementById("startTreatmentButton");
    const treatmentPatientInfo = document.getElementById("treatmentPatientInfo");
    const completeTreatmentButton = document.getElementById("completeTreatmentButton");


    treatmentPatientsTable.addEventListener('click', (event) => {
        const targetRow = event.target.closest('tr'); // 클릭한 행 찾기

        if (targetRow) {
            // 이전 선택된 행의 'selected' 클래스 제거
            const previouslySelected = treatmentPatientsTable.querySelector('tr.selected');
            if (previouslySelected) {
                previouslySelected.classList.remove('selected');
            }

            // 현재 행에 'selected' 클래스 추가
            targetRow.classList.add('selected');

            // 선택된 행의 데이터 출력
            const cells = targetRow.querySelectorAll('td'); // 행의 모든 셀 가져오기
            const rowData = Array.from(cells).map(cell => cell.textContent.trim()); // 셀의 텍스트를 배열로 변환

            console.log("선택된 진료중 행의 데이터:", rowData); // 선택된 행의 데이터 출력
        }
    });

    // 진료 시작 버튼 클릭 이벤트
    startTreatmentButton.addEventListener("click", function () {
        const selectedRow = waitingPatientsTable.querySelector('tr.selected')
        console.log(selectedRow)


        Array.from(selectedRow.cells).forEach((cell, index) => {
            console.log(`셀 인덱스 ${index}:`, cell.textContent);
        });

        const chartNum = selectedRow.cells[1].textContent
        const paName = selectedRow.cells[2].textContent
        const receptionTime = selectedRow.cells[5].textContent


        // 선택된 행의 의사 정보를 가져옴
        const selectedDoctor = selectedRow.querySelector('select').value
        console.log('선택된 의사:', selectedDoctor) // 의사 확인 로그 추가

        // 선택된 환자 정보 콘솔 출력
        console.log('선택된 환자:', paName, '차트 번호:', chartNum)

        treatmentPatientInfo.textContent = `환자: ${paName}`
        const treatmentModal = new bootstrap.Modal(document.getElementById('treatmentModal'));
        treatmentModal.show()

        const confirmTreatmentBtn = document.getElementById("confirmTreatmentBtn")
        confirmTreatmentBtn.onclick = function () {
            const formatReceptionTimeForDB = (timeString) => {
                if (!timeString) return null
                const [amPm, time] = timeString.split(' ')
                const [hours, minutes] = time.split(':').map(Number)
                const hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours

                const now = new Date()
                now.setHours(hoursIn24Format + 9, minutes) // 9시간 더하기
                return now.toISOString()
            }

            const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime)

            // 세션 스토리지에서 진료 시작 시간 가져오기
            const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || []
            const existingPatient = treatmentPatients.find(patient => patient.chartNum === chartNum)
            let viTime = existingPatient ? existingPatient.viTime : null // viTime 수정

            if (!viTime) {
                viTime = new Date(); // 현재 시간
                viTime.setHours(viTime.getHours() + 9); // +9시간 추가
                viTime = viTime.toISOString(); // ISO 형식으로 변환
            }

            const patientData = {
                chartNum: chartNum,
                paName: paName,
                treatStatus: "2",
                receptionTime: formattedReceptionTimeForDB,
                mainDoc: selectedDoctor, // 의사 정보
                viTime: viTime || new Date().toISOString() // viTime 수정
            }

            console.log('저장할 환자 데이터:', patientData)

            fetch("/api/patient-admission/treatment/start", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(patientData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(err => {
                            throw new Error(err.message || '진료 시작 실패')
                        })
                    }

                    // 진료 중 환자 테이블에 추가
                    addPatientToTreatmentTable({
                        chartNum: chartNum,
                        paName: paName,
                        receptionTime: receptionTime,
                        selectedDoctor: selectedDoctor,
                        viTime: viTime // viTime 수정
                    })

                    // 대기 중 환자 테이블에서 해당 환자 제거
                    const rowIndex = Array.from(waitingPatientsTable.rows).indexOf(selectedRow)
                    deletePatientFromWaitingTable(rowIndex)

                    // 대기 중 세션에서 해당 환자 제거
                    const waitingPatients = JSON.parse(sessionStorage.getItem('waitingPatients')) || []
                    const updatedWaitingPatients = waitingPatients.filter(patient => patient.chartNum !== chartNum)
                    sessionStorage.setItem('waitingPatients', JSON.stringify(updatedWaitingPatients))

                    // 진료 중 세션에 환자 추가
                    const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || []

                    // 중복 확인 및 추가
                    const isPatientAlreadyInSession = treatmentPatients.some(patient => patient.chartNum === chartNum)
                    if (!isPatientAlreadyInSession) {
                        treatmentPatients.push(patientData)
                        sessionStorage.setItem('treatmentPatients', JSON.stringify(treatmentPatients)) // 세션에 저장
                    } else {
                        console.log("이미 진료 중인 환자입니다.")
                    }

                    const storedPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || []
                    console.log('진료 중 세션에 저장된 환자 데이터:', storedPatients)

                    // 모든 환자 데이터를 순회하며 선택된 의사를 확인
                    storedPatients.forEach(patient => {
                        console.log(`차트 번호: ${patient.chartNum}, 선택된 의사: ${patient.selectedDoctor}`)
                    })

                    updateWaitingPatientCount()
                    alert("환자 진료가 시작되었습니다.")
                    treatmentModal.hide()
                })
                .catch(error => {
                    console.error("에러 발생:", error)
                    alert("진료 시작 중 오류가 발생했습니다: " + error.message)
                });
        }
    });
    // 모달 닫을 때 초기화
    const treatmentModalElement = document.getElementById('treatmentModal');
    treatmentModalElement.addEventListener('hidden.bs.modal', function () {
        treatmentPatientInfo.textContent = ''; // 모달이 닫힐 때 환자 정보 초기화
    });





    function formatCompleteTime(completionTime) {
        console.log("입력된 진료 완료 시간: ", completionTime);
        if (!completionTime || completionTime === 'N/A') {
            console.log("진료 완료 시간이 유효하지 않음: ", completionTime);
            return 'N/A';
        }

        const date = new Date(completionTime); // completionTime을 Date 객체로 변환
        if (isNaN(date.getTime())) {
            console.log("진료 완료 시간이 잘못된 형식: ", completionTime);
            return 'N/A'; // 유효하지 않은 날짜 처리
        }

        // UTC+6 시간으로 변환
        const utcOffset = 6 * 60 * 60 * 1000; // 6시간을 밀리초로 변환
        const localDate = new Date(date.getTime() + utcOffset);

        let hours = localDate.getHours();
        const minutes = localDate.getMinutes().toString().padStart(2, '0'); // 분을 2자리로 포맷팅
        const amPm = hours >= 12 ? '오후' : '오전'; // 오전/오후 구분
        hours = hours % 12 || 12; // 12시간 형식으로 변환

        const formattedTime = `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
        console.log("포맷된 진료 완료 시간: ", formattedTime);
        return formattedTime; // 포맷팅된 시간 반환
    }



    // 진료 완료 버튼 클릭 시 모달 표시
    completeTreatmentButton.addEventListener("click", function () {
        const selectedRow = treatmentPatientsTable.querySelector('tr.selected');
        console.log(selectedRow);

        // 선택된 행이 없을 경우 경고 메시지
        if (!selectedRow) {
            console.log("선택된 환자가 없습니다.");
            alert("진료를 완료할 환자를 선택해 주세요.");
            return; // 선택된 환자가 없으면 종료
        }

        // 선택된 행의 셀 수 확인
        const cellCount = selectedRow.cells.length;
        console.log("현재 셀 수:", cellCount);

        // 셀 수가 부족할 경우 처리
        if (cellCount < 6) {
            alert("셀 수가 부족합니다. 현재 셀 수: " + cellCount);
            return; // 셀 수가 부족하면 종료
        }

        for (let i = 0; i < cellCount; i++) {
            console.log(`셀 ${i} 내용:`, selectedRow.cells[i].textContent);
        }
        // 셀에서 데이터 가져오기
        const chartNum = selectedRow.cells[1].textContent;
        const paName = selectedRow.cells[2].textContent;
        const selectedDoctor = selectedRow.cells[3].textContent;
        const receptionTime = selectedRow.cells[4].textContent;
        const viTime = selectedRow.cells[5].textContent;



        console.log("차트 번호:", chartNum);
        console.log("환자 이름:", paName);
        console.log("주치의:", selectedDoctor);
        console.log("접수 시간:", receptionTime);
        console.log("completionTime:", viTime);

        // viTime 값 변환
        const formattedViTimeForDB = formatViTimeForDB(viTime);
        console.log("변환된 viTime 값:", formattedViTimeForDB);


        // receptionTime 변환
        const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime);
        console.log("변환된 receptionTime 값:", formattedReceptionTimeForDB); // 변환된 receptionTime 로그 출력

        // 환자 이름과 확인 메시지 설정
        const completeInfo = document.getElementById('completeInfo');
        completeInfo.textContent = `환자: ${paName}`; // 환자 이름과 함께 메시지 표시

        // 모달 보이기
        const completeModal = new bootstrap.Modal(document.getElementById('completeModal'));
        completeModal.show();

        const confirmCompleteButton = document.getElementById("confirmCompleteButton");
        confirmCompleteButton.onclick = function () {
            // 현재 시간으로 completionTime 생성 (UTC+9로 조정)
            const completionTime = new Date(); // 현재 시간 가져오기

            const formattedCompletionTimeValue = formatCompleteTime(completionTime);
            console.log("진료 완료 시간:", formattedCompletionTimeValue);

            // DB에 저장할 데이터 준비
            const dataToSend = {
                chartNum,
                paName,
                treatStatus: "3", // 진료 완료 상태
                mainDoc: selectedDoctor,
                receptionTime: formattedReceptionTimeForDB, // 변환된 receptionTime 사용
                viTime:formattedViTimeForDB, // viTime을 여기에서 사용
                completionTime:completionTime.toISOString() // 추가된 진료 완료 시간
            };

            console.log("저장할 데이터:", dataToSend);

            // API 호출
            fetch("/api/patient-admission/completeTreatment", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(dataToSend)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(err => {
                            throw new Error(err.message || '진료 완료 처리 실패');
                        });
                    }

                    // 진료 완료 후 환자 정보를 진료 완료 테이블에 추가
                    addPatientToCompleteTable({
                        chartNum: chartNum,
                        paName: paName,
                        selectedDoctor: selectedDoctor,
                        receptionTime: formattedReceptionTimeForDB,
                        completionTime:formattedCompletionTimeValue  // 필요하다면 추가
                    });

                    // 진료 중 환자 테이블에서 해당 환자 제거
                    const rowIndex = Array.from(treatmentPatientsTable.rows).indexOf(selectedRow);
                    deletePatientFromTreatmentTable(rowIndex); // 선택된 환자 행 삭제

                    // 진료 중 세션에서 해당 환자 제거
                    const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];
                    const updatedTreatmentPatients = treatmentPatients.filter(patient => patient.chartNum !== chartNum);
                    sessionStorage.setItem('treatmentPatients', JSON.stringify(updatedTreatmentPatients));

                    // 진료 완료 세션에 환자 추가
                    const completedPatients = JSON.parse(sessionStorage.getItem('completedPatients')) || [];
                    completedPatients.push({
                        chartNum,
                        paName,
                        selectedDoctor,
                        receptionTime: formattedReceptionTimeForDB,
                        completionTime:completionTime  // 진료 완료 시간 추가 (UTC+9로 조정됨)
                    });
                    sessionStorage.setItem('completedPatients', JSON.stringify(completedPatients));

                    // 대기 환자 수 업데이트
                    updateWaitingPatientCount();
                    alert("환자 진료가 완료되었습니다.");
                    completeModal.hide();
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("진료 완료 처리 중 오류가 발생했습니다: " + error.message);
                });
        };

        // 모달이 닫힐 때 patientInfo 초기화
        const completeModalElement = document.getElementById('completeModal');
        completeModalElement.addEventListener('hidden.bs.modal', function () {
            completeInfo.textContent = ''; // 모달이 닫힐 때 초기화
        });
    });



    const formatViTimeForDB = (timeString) => {
        if (!timeString) return null;

        // 예: '오후 03시 24분' 형식의 문자열
        const timePattern = /^(오전|오후) (\d{1,2}):(\d{1,2})$/;
        const match = timeString.match(timePattern);
        if (!match) {
            console.error(`시간 형식이 잘못되었습니다: ${timeString}`);
            return null; // 형식이 잘못된 경우
        }

        const amPm = match[1];
        const hours = parseInt(match[2], 10);
        const minutes = parseInt(match[3], 10);

        // 24시간 형식으로 변환
        let hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

        // 현재 날짜를 가져와서 시간을 설정
        const now = new Date();
        now.setHours(hoursIn24Format, minutes); // 초와 밀리초는 0으로 설정
        const utcOffset = 9 * 60; // 9시간을 분으로 변환
        const adjustedDate = new Date(now.getTime() + utcOffset * 60 * 1000); // UTC+9로 조정

        // ISO 문자열 반환
        return adjustedDate.toISOString(); // 'YYYY-MM-DDTHH:mm:ss.sssZ' 형식으로 반환
    };


    const formatReceptionTimeForDB = (timeString) => {
        if (!timeString) return null;

        // 예: '오후 04시 13분' 형식의 문자열
        const timePattern = /^(오전|오후) (\d{1,2}):(\d{1,2})$/;
        const match = timeString.match(timePattern);
        if (!match) {
            console.error(`시간 형식이 잘못되었습니다: ${timeString}`);
            return null; // 형식이 잘못된 경우
        }

        const amPm = match[1];
        const hours = parseInt(match[2], 10);
        const minutes = parseInt(match[3], 10);

        // 24시간 형식으로 변환
        let hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

        // 현재 날짜를 가져와서 시간을 설정
        const now = new Date();
        now.setHours(hoursIn24Format, minutes); // 초와 밀리초는 0으로 설정

        // ISO 문자열 반환 (UTC+9를 고려하여 시간 조정)
        const utcOffset = 9 * 60; // 9시간을 분으로 변환
        const adjustedDate = new Date(now.getTime() + utcOffset * 60 * 1000); // UTC+9로 조정
        return adjustedDate.toISOString(); // 'YYYY-MM-DDTHH:mm:ss.sssZ' 형식으로 반환
    };





    // 환자를 진료 중 환자 테이블에서 삭제하는 함수
    function deletePatientFromTreatmentTable(rowIndex) {
        if (rowIndex > -1) {
            treatmentPatientsTable.deleteRow(rowIndex); // 행 삭제
            console.log("행이 삭제되었습니다. 인덱스:", rowIndex);

            // 카운트 감소
            treatmentPatientCount--;
            console.log("현재 진료 중 환자 수:", treatmentPatientCount); // 현재 환자 수 확인

            // 헤더 업데이트
            updateTreatmentPatientCount(); // 환자 수 업데이트
        } else {
            console.log("행을 삭제할 수 없습니다. 유효하지 않은 인덱스입니다.");
        }
    }


// 대기 중 테이블에서 환자 삭제
    function deletePatientFromWaitingTable(rowIndex) {
        if (waitingPatientsTable.rows.length > rowIndex && rowIndex >= 0) {
            waitingPatientsTable.deleteRow(rowIndex); // 선택한 행 삭제
            updateRowNumbers(); // 삭제 후 행 번호 업데이트
            updateWaitingPatientCount(); // 대기 환자 수 업데이트
        } else {
            console.error(`Invalid row index: ${rowIndex}`); // 유효하지 않은 행 인덱스 오류 메시지
        }
    }


// 행 번호 업데이트
    function updateRowNumbers() {
        const rows = waitingPatientsTable.rows;
        for (let i = 0; i < rows.length; i++) {
            rows[i].cells[0].innerText = String(i + 1); // 현재 행 번호로 첫 번째 셀 업데이트
        }
    }

    // 대기 중 테이블에 환자 추가
    function addPatientToWaitingTable(patient) {
        // 테이블의 현재 행 개수를 기반으로 번호를 설정
        const row = waitingPatientsTable.insertRow();
        const currentRowCount = waitingPatientsTable.rows.length; // 현재 행 수

        row.innerHTML = `

    <td>${currentRowCount}</td> <!-- 새로운 행 번호는 현재 테이블의 행 수 -->
    <td>${patient.chartNum || 'N/A'}</td>
    <td>${patient.paName || 'N/A'}</td>
    <td>
        <select>
            <option value="의사1">의사1</option>
            <option value="의사2">의사2</option>
            <option value="의사3">의사3</option>
        </select>
    </td>
    <td>${patient.rvTime ? new Date(patient.rvTime).toLocaleString() : 'N/A'}</td>
    <td>${patient.receptionTime ? new Date(patient.receptionTime).toLocaleTimeString([], {
            hour: '2-digit',
            minute: '2-digit'
        }) : 'N/A'}</td>
    `;

        row.addEventListener('click', () => {
            const previouslySelected = waitingPatientsTable.querySelector('tr.selected');
            if (previouslySelected) {
                previouslySelected.classList.remove('selected');
            }
            row.classList.add('selected');
        });
        const existingPatients = JSON.parse(sessionStorage.getItem('waitingPatients')) || [];
        existingPatients.push(patient); // 새로운 환자 추가
        sessionStorage.setItem('waitingPatients', JSON.stringify(existingPatients));

        updateWaitingPatientCount(); // 대기 환자 수 업데이트
    }

    // 페이지가 로드될 때 기존 환자 정보 로드
    window.addEventListener('load', () => {
        const waitingPatients = JSON.parse(sessionStorage.getItem('waitingPatients')) || [];

        // 세션 스토리지에 저장된 모든 환자 정보가 있는 경우 테이블에 추가
        waitingPatients.forEach(patient => {
            const existingRows = Array.from(waitingPatientsTable.rows);
            const alreadyExists = existingRows.some(row => {
                return row.cells[1].innerText === patient.chartNum; // 차트 번호로 중복 검사
            });

            // 환자 정보가 이미 대기 중이지 않으면 테이블에 추가
            if (!alreadyExists) {
                addPatientToWaitingTable({
                    chartNum: patient.chartNum || 'N/A',
                    paName: patient.paName || 'N/A',
                    mainDoc: patient.mainDoc || null,
                    rvTime: patient.rvTime || null,
                    receptionTime: patient.receptionTime || new Date().toISOString(),
                    treatStatus: "1" // 대기 상태
                });
            }
        });

        // 대기 환자 수 업데이트
        updateWaitingPatientCount();
    });




    // 현재 시간을 포맷팅하는 함수
    const formatCurrentTime = (date) => {
        let hours = date.getHours();
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const amPm = hours >= 12 ? '오후' : '오전';
        hours = hours % 12 || 12; // 12시간 형식으로 변환
        return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
    };

// 접수 시간을 포맷팅하는 함수
    const formatReceptionTime = (dateTimeString) => {
        if (!dateTimeString || dateTimeString === 'N/A') return 'N/A'; // 유효하지 않은 시간 문자열 처리
        console.log('formatReceptionTime에 입력된 값:', dateTimeString); // 입력 확인

        // ISO 형식인지 확인
        if (dateTimeString.includes('T')) {
            const date = new Date(dateTimeString);
            return formatCurrentTime(date); // ISO 형식일 때 포맷팅하여 반환
        }

        // ISO 형식이 아닌 경우, 기존 로직 사용
        const parts = dateTimeString.split(' '); // period와 time 분리
        if (parts.length !== 2) {
            console.error('시간 형식이 잘못되었습니다:', dateTimeString);
            return 'N/A'; // 형식 오류 처리
        }

        const [period, time] = parts; // period와 time 분리
        console.log('오전/오후:', period, '시간:', time); // 분리된 값 확인

        // 시간과 분 파싱
        let [hours, minutes] = time.split(':').map(Number); // 시간과 분을 숫자로 변환
        console.log('파싱된 시간:', hours, '파싱된 분:', minutes); // 파싱된 값 확인

        // 유효성 검사
        if (isNaN(hours) || isNaN(minutes)) {
            console.error('유효하지 않은 시간 또는 분:', hours, minutes);
            return 'N/A'; // 유효하지 않은 경우 N/A 반환
        }

        // 오전/오후에 따른 12시간 형식 변환
        if (period === '오후' && hours < 12) {
            hours += 12; // 오후일 때 12를 더함
        } else if (period === '오전' && hours === 12) {
            hours = 0; // 오전 12시는 0으로 변환
        }

        // 날짜 객체 생성
        const date = new Date();
        date.setHours(hours, minutes);

        const formattedTime = formatCurrentTime(date);
        console.log('포맷된 접수 시간:', formattedTime); // 포맷된 접수 시간 확인
        return formattedTime; // 포맷된 시간 반환
    };

    function addPatientToTreatmentTable(patient) {
        console.log('진료 중 환자 테이블에 추가하는 환자:', patient); // 데이터 확인

        const row = treatmentPatientsTable.insertRow(); // 진료 중 테이블에 새 행 추가
        treatmentPatientCount++; // 진료 중 환자 수 증가
        console.log('현재 진료 중 환자 수:', treatmentPatientCount); // 환자 수 확인

        const formattedReceptionTime = formatReceptionTime(patient.receptionTime); // 접수 시간을 포맷팅
        const treatmentStartTime = patient.viTime
            ? formatCurrentTime(new Date(new Date(patient.viTime).setHours(new Date(patient.viTime).getHours() + 15)))
            : formatCurrentTime(new Date()); // 진료 시작 시간 포맷팅
        console.log("세션 스토리지에서 가져온 treatmentPatients:", patient.viTime);


        console.log('포맷된 접수 시간:', formattedReceptionTime); // 포맷된 접수 시간 확인

        // 새 행의 내용 설정
        row.innerHTML = `
        <td>${treatmentPatientCount}</td> <!-- 여기에 treatmentPatientCount 사용 -->
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td><!-- 포맷된 접수 시간 -->
        <td>${treatmentStartTime || 'N/A'}</td> <!-- 포맷된 진료 시작 시간 -->
    `;
        console.log('환자의 receptionTime:', patient.receptionTime);
        console.log('포맷된 receptionTime:', formattedReceptionTime);
        console.log('진료시작시간', treatmentStartTime);
        console.log('새로 추가된 행:', row); // 추가된 행 정보 확인
        updateTreatmentPatientCount(); // 환자 수 업데이트

        // 행 선택 기능 추가
        row.addEventListener('click', () => {
            const previouslySelected = treatmentPatientsTable.querySelector('tr.selected');
            if (previouslySelected) {
                previouslySelected.classList.remove('selected'); // 이전 선택된 행에서 클래스를 제거
            }
            row.classList.add('selected'); // 현재 클릭된 행에 클래스를 추가
        });
    }


    window.addEventListener('load', () => {
        const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];

        treatmentPatients.forEach(patient => {
            const existingRows = Array.from(treatmentPatientsTable.rows);
            const alreadyExists = existingRows.some(row => {
                return row.cells[1].innerText === patient.chartNum; // 차트 번호로 중복 검사
            });

            if (!alreadyExists) {
                // 접수 시간에 15시간 추가
                const receptionTime = new Date(patient.receptionTime);
                const adjustedReceptionTime = new Date(receptionTime);
                adjustedReceptionTime.setHours(adjustedReceptionTime.getHours() + 15);
                const formattedReceptionTime = adjustedReceptionTime.toISOString();

                // viTime 처리
                let viTime = patient.viTime;

                // viTime이 없거나 null인 경우, 현재 시간으로 설정
                if (!viTime) {

                } else {
                    // viTime이 문자열인 경우, Date 객체로 변환
                    if (typeof viTime === 'string') {
                        viTime = new Date(viTime);
                    }
                    // Date 객체를 ISO 형식으로 변환
                    viTime = viTime instanceof Date && !isNaN(viTime) ? viTime.toISOString() : 'N/A';
                }

                // 콘솔에 viTime 출력
                console.log("세션에서 가져온 viTime:", viTime);

                // 환자 정보를 테이블에 추가
                addPatientToTreatmentTable({
                    chartNum: patient.chartNum || 'N/A',
                    paName: patient.paName || 'N/A',
                    selectedDoctor: patient.mainDoc || 'N/A',
                    receptionTime: formattedReceptionTime,
                    viTime: viTime, // ISO 형식으로 변환된 viTime 사용
                    treatStatus: "2" // 진료 중 상태
                });
            } else {
                // 이미 존재하는 환자일 경우 viTime 업데이트
                const existingRow = existingRows.find(row => row.cells[1].innerText === patient.chartNum);
                if (existingRow) {
                    existingRow.cells[5].innerText = patient.viTime; // 세션에서 가져온 진료 시작 시간으로 업데이트
                }
            }
        });

        // 진료 중 환자 수 업데이트
        updateTreatmentPatientCount();
    });




// 진료 완료 환자를 완료 테이블에 추가하는 함수
    function addPatientToCompleteTable(patient) {
        const completePatientsTable = document.getElementById('completedPatientsTable');
        if (!completePatientsTable) {
            console.error("Completed Patients Table not found!");
            return;
        }

        const row = completePatientsTable.insertRow();
        completePatientCount++;
        console.log("Patient's reception time:", patient.receptionTime);

        // 접수 시간 UTC+6으로 변환
        const formatReceptionTime = (dateTimeString) => {
            if (!dateTimeString || dateTimeString === 'N/A') return 'N/A';
            const date = new Date(dateTimeString);
            // UTC+6 시간으로 조정
            const utcOffset = 15 * 60 * 60 * 1000; // 15시간을 밀리초로 변환
            const convertedDate = new Date(date.getTime() + utcOffset);

            if (isNaN(convertedDate)) return 'N/A'; // 유효하지 않은 날짜 처리

            let hours = convertedDate.getHours();
            const minutes = convertedDate.getMinutes().toString().padStart(2, '0');
            const amPm = hours >= 12 ? '오후' : '오전';
            hours = hours % 12 || 12;
            return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`; // 포맷팅된 시간 반환
        };

        // 진료 완료 시간 UTC+15으로 변환
        const formatCompleteTime = (completionTime) => {
            if (!completionTime || completionTime === 'N/A') return 'N/A'; // 값이 없거나 'N/A'일 경우

            // completionTime이 "오전 01:32" 형식일 경우
            if (typeof completionTime === 'string' && /[오전|오후]/.test(completionTime)) {
                const [amPm, timePart] = completionTime.split(" "); // 오전/오후와 시간 부분을 분리
                const [hoursStr, minutesStr] = timePart.split(":"); // 시와 분 분리


                const hours = parseInt(hoursStr, 10); // 문자열을 정수로 변환
                const minutes = parseInt(minutesStr, 10); // 문자열을 정수로 변환
                console.log("시간 부분:", timePart, "오전/오후:", amPm);
                console.log("시간:", hoursStr, "분:", minutesStr);
                console.log("hours:", hours, "minutes:", minutes);

                // 12시간 형식을 24시간 형식으로 변환
                let convertedHours = amPm === '오후' && hours < 12 ? hours + 12 : hours;
                convertedHours = amPm === '오전' && hours === 12 ? 0 : convertedHours; // 12 AM은 0시로 변환

                const date = new Date();
                date.setHours(convertedHours);
                date.setMinutes(minutes);
                date.setSeconds(0);

                // UTC+6 시간으로 변환
                const utcOffset = 24 * 60 * 60 * 1000; // 6시간을 밀리초로 변환
                const localDate = new Date(date.getTime() + utcOffset); // UTC 시간에 오프셋 추가

                let finalHours = localDate.getHours(); // 지역 시간의 시를 가져옴
                const finalMinutes = localDate.getMinutes().toString().padStart(2, '0'); // 분을 2자리로 포맷팅
                const finalAmPm = finalHours >= 12 ? '오후' : '오전'; // 오전/오후 구분
                finalHours = finalHours % 12 || 12; // 12시간 형식으로 변환

                return `${finalAmPm} ${finalHours.toString().padStart(2, '0')}:${finalMinutes}`; // 포맷팅된 시간 반환
            }

            // completionTime이 ISO 8601 형식일 경우 처리
            const date = new Date(completionTime);

            if (isNaN(date.getTime())) return 'N/A'; // 유효하지 않은 날짜 처리

            // UTC+6 시간으로 변환
            const utcOffset = 24 * 60 * 60 * 1000; // 6시간을 밀리초로 변환
            const localDate = new Date(date.getTime() + utcOffset); // UTC 시간에 오프셋 추가

            let hours = localDate.getHours(); // 지역 시간의 시를 가져옴
            const minutes = localDate.getMinutes().toString().padStart(2, '0'); // 분을 2자리로 포맷팅
            const amPm = hours >= 12 ? '오후' : '오전'; // 오전/오후 구분
            hours = hours % 12 || 12; // 12시간 형식으로 변환

            return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`; // 포맷팅된 시간 반환
        };




        console.log("전송할 진료 완료 시간:", patient.completionTime);

        // completionTime 값을 가져와서 포맷팅
        const formattedCpTime = formatCompleteTime(patient.completionTime);
        console.log('포맷된 진료 완료 시간', formattedCpTime);

        const formattedReceptionTime = formatReceptionTime(patient.receptionTime);

        row.innerHTML = `
        <td>${completePatientCount}</td>
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td>
        <td>${formattedCpTime || 'N/A'}</td>
    `;


        console.log('진료 완료 시간', formattedCpTime);
        // 완료 환자 수 업데이트
        updateCompletePatientCount();
    }

// 페이지 로드 시 completedPatients에서 진료 완료 시간 표시
    window.addEventListener('load', function () {
        const completedPatients = JSON.parse(sessionStorage.getItem('completedPatients')) || [];

        completedPatients.forEach(patient => {
            // 접수 시간을 포맷팅
            const formattedReceptionTime = formatReceptionTime(patient.receptionTime);
            // completionTime은 세션 스토리지에서 읽어와서 사용
            const completionTime = patient.completionTime;
            // addPatientToCompleteTable 함수로 진료 완료 테이블에 환자 추가
            addPatientToCompleteTable({
                chartNum: patient.chartNum || 'N/A',
                paName: patient.paName || 'N/A',
                selectedDoctor: patient.selectedDoctor || 'N/A',
                receptionTime: patient.receptionTime || 'N/A',
                completionTime: completionTime // 진료 완료 시간
            });

            console.log('진료완료 접수시간',patient.receptionTime)
        });
    });





    function updateWaitingPatientCount() {
        const count = waitingPatientsTable.rows.length ;
        const header = document.querySelector("#waitingPatientsTable th[colspan='6']");
        header.textContent = `진료 대기 환자: ${count}명`;

    }

    function updateTreatmentPatientCount() {
        const count = treatmentPatientsTable.rows.length; // 현재 테이블의 행 개수
        const header = document.querySelector("#treatmentPatientsTable th[colspan='6']");

        if (header) {
            header.textContent = `진료 중 환자: ${count}명`; // 헤더 업데이트
        } else {
            console.error("헤더를 찾을 수 없습니다. 선택자를 확인하세요.");
        }
    }

    // 진료 완료 환자 수 업데이트하는 함수
    function updateCompletePatientCount() {
        const header = document.querySelector('#completedPatientsTable th[colspan="6"]');
        header.textContent = `진료 완료 환자: ${completePatientCount}명`;
    }


    // // 매 5초마다 환자 목록 갱신
    // setInterval(() => {
    //     const selectedDate = new Date(document.getElementById('currentDate').value);
    //     fetchAndDisplayPatients(selectedDate); // 현재 선택된 날짜의 환자 목록 갱신
    // }, 5000);

});


document.addEventListener("DOMContentLoaded", function () {
    const waitingPatientsTable = document.getElementById("waitingPatientsTable").getElementsByTagName("tbody")[0];
    let selectedRow = null; // 선택된 행을 저장할 변수

    // 모달 요소들
    const cancelModal = new bootstrap.Modal(document.getElementById('cancelModal'));
    const patientNameElement = document.getElementById("patientName");
    const confirmCancelBtn = document.getElementById("confirmCancelBtn");
    const closeModalBtn = document.querySelector('#cancelModal .btn-secondary'); // '아니요' 버튼
    const cancelReceptionButton = document.getElementById("cancelReceptionButton");

    // 환자 수 업데이트 함수
    function updateWaitingPatientCount() {
        const count = waitingPatientsTable.rows.length;
        const header = document.querySelector("#waitingPatientsTable th[colspan='6']");
        header.textContent = `진료 대기 환자: ${Math.max(count, 0)}명`;
    }

    function updateRowIndexes() {
        const rows = waitingPatientsTable.getElementsByTagName("tr");
        for (let i = 0; i < rows.length; i++) {
            const cell = rows[i].cells[0]; // 첫 번째 열(번호 열)을 가져옵니다.
            cell.textContent = String(i + 1); // i + 1로 행 번호 업데이트
        }
    }

    // 대기 환자 테이블의 행 클릭 시 해당 행 선택
    waitingPatientsTable.addEventListener("click", function (event) {
        const targetRow = event.target.closest("tr"); // 클릭한 셀의 행을 가져옴
        if (!targetRow) return;

        if (selectedRow) {
            selectedRow.classList.remove("clicked"); // 이전 행의 선택 클래스 제거
        }

        // 선택된 행 저장
        selectedRow = targetRow;
        selectedRow.classList.add("clicked");

        console.log("Selected patient:", selectedRow.cells[2].textContent);
    });

    // 접수 취소 버튼 클릭 시 모달 표시
    cancelReceptionButton.addEventListener("click", function () {
        if (selectedRow) {
            const patientName = selectedRow.cells[2].textContent;
            patientNameElement.textContent = `환자 이름: ${patientName}`;
            cancelModal.show();
        } else {
            alert("취소할 환자를 먼저 선택하세요.");
        }
    });

    // 모달에서 '예' 버튼 클릭 시 행 삭제
    confirmCancelBtn.addEventListener("click", function () {
        if (selectedRow) {
            selectedRow.remove(); // 선택된 행 삭제
            updateRowIndexes(); // 행 번호 업데이트
            updateWaitingPatientCount(); // 환자 수 업데이트
            selectedRow = null; // 선택된 행 초기화
        }

        // 모달 닫기
        cancelModal.hide();
    });

    // '아니요' 버튼 클릭 시 모달 닫기
    closeModalBtn.addEventListener("click", function () {
        cancelModal.hide();
    });
});


// 날짜를 선택할 때 환자 정보를 가져오는 이벤트 리스너
document.getElementById('currentDate').addEventListener('change', function () {
    const selectedDate = this.value; // 선택한 날짜 가져오기
    console.log('선택한 날짜:', selectedDate); // 선택한 날짜 로그

    // API 호출하여 환자 정보를 가져오기
    fetch(`/api/patient-admission/date/${selectedDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            return response.json();
        })
        .then(data => {
            // 각 테이블의 본문 비우기
            const waitingPatientsBody = document.getElementById('waitingPatientsBody');
            waitingPatientsBody.innerHTML = '';

            const treatmentPatientsBody = document.getElementById('treatmentPatientsBody');
            console.log(treatmentPatientsBody);
            treatmentPatientsBody.innerHTML = '';

            const completedPatientsBody = document.getElementById('completedPatientsBody');
            completedPatientsBody.innerHTML = '';

            // 환자 데이터를 각 테이블에 추가
            data.forEach(patient => {
                const formattedReceptionTime = patient.receptionTime ? new Date(patient.receptionTime).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                }) : 'N/A';
                const formattedRvTime = patient.rvTime ? new Date(patient.rvTime).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                }) : 'N/A';
                const formattedViTime = patient.viTime ? new Date(patient.viTime).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                }) : 'N/A';
                const formattedCpTime = patient.completionTime ? new Date(patient.completionTime).toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit'
                }) : 'N/A';

                // 대기환자 추가
                if (patient.treatStatus === '1') { // 대기 상태 코드
                    waitingPatientsBody.innerHTML += `
                        <tr>
                            <td>${waitingPatientsBody.children.length + 1}</td> <!-- 카운트 증가 -->
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>${patient.mainDoc || 'N/A'}</td> <!-- 의사 이름 -->
                            <td>${formattedRvTime}</td>
                            <td>${formattedReceptionTime}</td>
                        </tr>
                    `;
                    console.log("대기 환자 추가:", patient); // 디버깅을 위한 로그
                }

                // 진료 중 환자 추가
                if (patient.treatStatus === '2') { // 진료 중 상태 코드
                    treatmentPatientsBody.innerHTML += `
                        <tr>
                            <td>${treatmentPatientsBody.children.length + 1}</td> <!-- 카운트 증가 -->
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>${patient.mainDoc || 'N/A'}</td>
                            <td>${formattedRvTime}</td>
                            <td>${formattedViTime}</td>
                            <td>${formattedReceptionTime}</td>
                        </tr>
                    `;
                    console.log("진료 중 환자 추가:", patient); // 디버깅을 위한 로그
                }

                // 진료 완료 환자 추가
                if (patient.treatStatus === '3') { // 진료 완료 상태 코드
                    completedPatientsBody.innerHTML += `
                        <tr>
                            <td>${completedPatientsBody.children.length + 1}</td> <!-- 카운트 증가 -->
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>${patient.mainDoc || 'N/A'}</td>
                            <td>${formattedReceptionTime}</td>
                            <td>${formattedCpTime}</td> <!-- 진료 완료 시간 -->
                        </tr>
                    `;
                    console.log("진료 완료 환자 추가:", patient); // 디버깅을 위한 로그
                }
            });
            updateWaitingPatientCount();
            updateTreatmentPatientCount();
            updateCompletePatientCount();
        })
        .catch(error => {
            console.error('문제가 발생했습니다:', error);
        });

    function updateWaitingPatientCount() {
        // 대기 환자 테이블의 행 개수 (헤더 제외)
        const count = waitingPatientsBody.rows.length;
        // 테이블 헤더 선택
        const header = document.querySelector("#waitingPatientsTable th[colspan='6']");
        // 헤더에 환자 수 업데이트
        header.textContent = `진료 대기 환자: ${count}명`;
    }

// 진료 중 환자 수 업데이트 함수
    function updateTreatmentPatientCount() {
        const count = treatmentPatientsBody.rows.length; // 진료 중 환자 수
        const header = document.querySelector("#treatmentPatientsTable th[colspan='7']");
        header.textContent = `진료 중 환자: ${count}명`;
    }

// 진료 완료 환자 수 업데이트 함수
    function updateCompletePatientCount() {
        const count = completedPatientsBody.rows.length; // 진료 완료 환자 수
        const header = document.querySelector("#completedPatientsTable th[colspan='6']");
        header.textContent = `진료 완료 환자: ${count}명`;
    }
});




// 날짜 선택 시 이벤트 리스너
document.getElementById('currentDate').addEventListener('change', function () {
    const selectedDate = this.value; // 선택한 날짜 가져오기
    console.log('선택한 날짜:', selectedDate); // 선택한 날짜 로그

    fetch(`/api/patient-admission/date/${selectedDate}`)
        .then(response => {
            console.log('응답 상태:', response.status); // 응답 상태 로그
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            return response.json();
        })
        .then(data => {
            console.log('가져온 데이터:', data); // 가져온 데이터 로그
            // 테이블 업데이트 부분
            // (여기에 값을 업데이트하는 로직이 있어야 합니다)
        })
        .catch(error => {
            console.error('문제가 발생했습니다:', error);
        });
});


