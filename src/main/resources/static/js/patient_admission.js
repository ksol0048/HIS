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

    // 날짜 변경 이벤트
    document.getElementById('currentDate').addEventListener("change", function () {
        const selectedDate = new Date(this.value);
        selectedDate.setHours(0, 0, 0, 0); // 선택한 날짜의 시간 부분을 00:00:00으로 설정
        fetchAndDisplayPatients(selectedDate); // 날짜가 바뀌면 환자 목록 갱신
    });

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
                chartNum: selectedPatient.chartNum,
                paName: selectedPatient.name,
                mainDoc: null,  // 의사는 null로 설정
                rvTime: selectedPatient.rvTime,
            };

            // API 호출하여 환자 접수
            fetch("/api/patient-admission/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(patientData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => { // 응답 텍스트로 오류 메시지 처리
                            throw new Error(text || '환자 접수 실패');
                        });
                    }
                    return response.json(); // JSON으로 응답을 파싱
                })
                .then(data => {
                    console.log(data);
                    // 대기 중 테이블에 추가 (여기서 data는 등록된 환자 정보)
                    addPatientToWaitingTable({
                        ...patientData,
                        receptionTime: new Date().toISOString(),
                        treatStatus: "1" // 대기 상태
                    });
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("환자 접수 중 오류가 발생했습니다. 다시 시도해 주세요.");
                });
        } else {
            console.log("세션에서 환자 정보가 없습니다.");
            alert("선택된 환자가 없습니다.");
        }
    });

    const startTreatmentButton = document.getElementById("startTreatmentButton");
    const treatmentModal = new bootstrap.Modal(document.getElementById('treatmentModal'));
    const treatmentPatientInfo = document.getElementById("treatmentPatientInfo");
    const completeTreatmentButton = document.getElementById("completeTreatmentButton");

    // // 접수 시간을 포맷하는 함수 정의
    // const formatReceptionTime = (dateTimeString) => {
    //     if (!dateTimeString) return 'N/A'; // 유효하지 않은 시간 문자열 처리
    //     const date = new Date(dateTimeString);
    //     const formatCurrentTime = (date) => {
    //         let hours = date.getHours();
    //         const minutes = date.getMinutes().toString().padStart(2, '0');
    //         const amPm = hours >= 12 ? '오후' : '오전';
    //         hours = hours % 12 || 12; // 12시간 형식으로 변환
    //         return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
    //     };
    //     return isNaN(date.getTime()) ? 'N/A' : formatCurrentTime(date); // NaN 체크 추가
    // };


// 진료 시작 버튼 클릭 이벤트
    startTreatmentButton.addEventListener("click", function () {
        const selectedRow = waitingPatientsTable.querySelector('tr.selected');

        if (selectedRow) {
            const chartNum = selectedRow.cells[1].textContent;
            const paName = selectedRow.cells[2].textContent;
            const receptionTime = selectedRow.cells[5].textContent; // 대기 중 테이블에서 접수 시간 가져오기

            console.log('Adding patient to treatment table:', {
                chartNum: chartNum,
                paName: paName,
                receptionTime: receptionTime // 여기에서 접수 시간을 가져옴
            });

            const selectedDoctor = selectedRow.querySelector('select').value; // 선택한 의사 정보 가져오기

            // 모달에 환자 정보 설정
            treatmentPatientInfo.textContent = `환자: ${paName}`;

            // 모달 표시
            treatmentModal.show();

            // 모달의 "예" 버튼 클릭 이벤트 설정
            const confirmTreatmentBtn = document.getElementById("confirmTreatmentBtn");
            confirmTreatmentBtn.onclick = function () {
                // 접수 시간을 ISO 형식으로 변환하는 함수
                const formatReceptionTimeForDB = (timeString) => {
                    if (!timeString) return null; // 유효하지 않은 시간 문자열 처리

                    const [amPm, time] = timeString.split(' '); // "오후"와 시간 분리
                    const [hours, minutes] = time.split(':').map(Number);

                    // 12시간 형식에서 24시간 형식으로 변환
                    const hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

                    // 현재 날짜와 시간을 사용하여 ISO 형식으로 변환
                    const now = new Date();
                    now.setHours(hoursIn24Format + 9, minutes); // 9시간 더하기
                    return now.toISOString(); // ISO 형식으로 변환
                };

                // 포맷된 접수 시간 생성
                const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime); // DB에 저장될 변환된 접수 시간

                const patientData = {
                    chartNum: chartNum,
                    paName: paName,
                    treatStatus: "2", // 진료중 상태
                    receptionTime: formattedReceptionTimeForDB, // ISO 형식의 접수 시간 추가 (DB 저장용)
                    mainDoc: selectedDoctor // 의사 정보 추가
                };

                // API 호출하여 환자 상태 변경
                fetch("/api/patient-admission/treatment/start", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(patientData) // 환자 데이터를 JSON으로 변환하여 전송
                })
                    .then(response => {
                        if (!response.ok) {
                            return response.json().then(err => {
                                throw new Error(err.message || '진료 시작 실패');
                            });
                        }

                        // 진료 중 환자 테이블에 추가
                        addPatientToTreatmentTable({
                            chartNum: chartNum,
                            paName: paName,
                            receptionTime: receptionTime, // 포맷된 접수 시간 (변환된 값)
                            selectedDoctor: selectedDoctor // 선택한 의사 정보 저장
                        });

                        // 대기 중 환자 테이블에서 해당 환자 제거
                        const rowIndex = Array.from(waitingPatientsTable.rows).indexOf(selectedRow);
                        deletePatientFromWaitingTable(rowIndex); // 선택된 행의 인덱스 계산

                        // 대기 환자 수 업데이트
                        updateWaitingPatientCount();

                        alert("환자 진료가 시작되었습니다."); // 사용자에게 성공 메시지 표시

                        // 모달 닫기
                        treatmentModal.hide();
                    })
                    .catch(error => {
                        console.error("에러 발생:", error);
                        alert("진료 시작 중 오류가 발생했습니다: " + error.message);
                    });
            };
        } else {
            console.log("선택된 환자가 없습니다."); // 직관적인 메시지
            alert("진료를 시작할 환자를 선택해 주세요."); // 사용자 안내 메시지
        }
    });


    // 진료 완료 버튼 클릭 시 모달 표시
    completeTreatmentButton.addEventListener("click", function () {
        const selectedRow = treatmentPatientsTable.querySelector('tr.selected'); // 선택된 행 확인
        console.log("선택된 환자:", selectedRow);

        if (selectedRow) {
            // 환자 정보를 추출
            const chartNum = selectedRow.cells[1].textContent;
            const paName = selectedRow.cells[2].textContent;
            const selectedDoctor = selectedRow.cells[3].textContent;
            const receptionTime = selectedRow.cells[5].textContent;
            const viTime = selectedRow.cells[6].textContent;

            // 모달에 환자 정보 표시
            document.getElementById("patientInfo").textContent = `환자 이름: ${paName}, 차트 번호: ${chartNum}`;

            // 모달 보여주기
            const completeModal = new bootstrap.Modal(document.getElementById('completeModal'));
            completeModal.show();

            // 예 버튼 클릭 시 API 호출 및 환자 삭제
            document.getElementById("confirmCompleteButton").onclick = function () {
                // DB에 저장할 데이터 준비
                const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime);
                const formattedViTimeForDB = formatVTimeForDB(viTime);

                // API 호출
                fetch("/api/patient-admission/completeTreatment", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        chartNum,
                        paName,
                        treatStatus: "3",
                        mainDoc: selectedDoctor,
                        receptionTime: formattedReceptionTimeForDB,
                        viTime: formattedViTimeForDB
                    })
                })
                    .then(response => {
                        if (!response.ok) {
                            return response.json().then(err => {
                                throw new Error(err.message || '진료 완료 처리 실패');
                            });
                        }

                        // 진료 중 환자 테이블에서 선택된 환자 제거
                        const rowIndex = Array.from(treatmentPatientsTable.rows).indexOf(selectedRow);
                        deletePatientFromTreatmentTable(rowIndex); // 선택된 환자 삭제

                        // 진료 완료 환자 테이블에 추가
                        addPatientToCompleteTable({
                            chartNum,
                            paName,
                            selectedDoctor,
                            receptionTime: formattedReceptionTimeForDB
                        });

                        // 환자 수 업데이트
                        updateTreatmentPatientCount();

                        alert("환자 진료가 완료되었습니다.");
                        completeModal.hide(); // 모달 닫기
                    })
                    .catch(error => {
                        console.error("에러 발생:", error);
                        alert("진료 완료 처리 중 오류가 발생했습니다: " + error.message);
                    });
            };
        } else {
            alert("진료를 완료할 환자를 선택해 주세요.");
        }
    });


    function getCurrentTreatmentPatients() {
        const currentPatients = [];
        for (let i = 1; i < treatmentPatientsTable.rows.length; i++) { // 첫 번째 행은 헤더이므로 건너뛰기
            const row = treatmentPatientsTable.rows[i];
            const patient = {
                index: i,
                chartNum: row.cells[1].textContent,
                paName: row.cells[2].textContent,
                selectedDoctor: row.cells[3].textContent,
                currentTime: row.cells[5].textContent,
                receptionTime: row.cells[6].textContent,
            };
            currentPatients.push(patient);
        }
        return currentPatients;
    }


    const formatReceptionTimeForDB = (timeString) => {
        if (!timeString) return null; // 유효하지 않은 시간 문자열 처리

        // "오후"와 시간 분리
        const parts = timeString.split(' '); // '오후 11:31'
        if (parts.length !== 2) {
            console.error("시간 형식이 잘못되었습니다:", timeString);
            return null; // 시간 형식 오류 처리
        }

        const amPm = parts[0]; // "오후"
        const time = parts[1]; // "11:31"
        const [hours, minutes] = time.split(':').map(Number);

        // 12시간 형식에서 24시간 형식으로 변환
        const hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

        const now = new Date();
        now.setHours(hoursIn24Format + 9, minutes); // 9시간 더하기 (시간, 분, 초, 밀리초)
        return now.toISOString(); // ISO 형식으로 변환
    };

    const formatVTimeForDB = (viTimeString) => {
        if (!viTimeString) return null; // 유효하지 않은 viTime 문자열 처리

        // viTime을 ISO 형식으로 변환하는 로직 (예시)
        const parts = viTimeString.split(' '); // '오후 11:31'
        if (parts.length !== 2) {
            console.error("viTime 형식이 잘못되었습니다:", viTimeString);
            return null; // viTime 형식 오류 처리
        }

        const amPm = parts[0]; // "오후"
        const time = parts[1]; // "11:31"
        const [hours, minutes] = time.split(':').map(Number);
        const hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

        const now = new Date();
        now.setHours(hoursIn24Format + 9, minutes); // 9시간 더하기
        return now.toISOString(); // ISO 형식으로 변환
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
            waitingPatientsTable.deleteRow(rowIndex);

            updateWaitingPatientCount();
        } else {
            console.error(`Invalid row index: ${rowIndex}`);
        }
    }

// // 대기 환자 테이블의 행 번호 업데이트
//     function updateRowNumbers() {
//         for (let i = 1; i < waitingPatientsTable.rows.length; i++) {
//             const cell = waitingPatientsTable.rows[i].cells[0]; // 첫 번째 셀 (환자 번호)
//             cell.textContent = String(i); // 행 번호 업데이트
//         }
//     }


    // 대기 중 테이블에 환자 추가
    function addPatientToWaitingTable(patient) {
        const row = waitingPatientsTable.insertRow();
        waitingPatientCount++;
        row.innerHTML = `
        <td>${waitingPatientCount}</td>
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

        updateWaitingPatientCount();
    }

    // 현재 시간을 포맷팅하는 함수
    const formatCurrentTime = (date) => {
        let hours = date.getHours();
        const minutes = date.getMinutes().toString().padStart(2, '0');
        const amPm = hours >= 12 ? '오후' : '오전';
        hours = hours % 12 || 12; // 12시간 형식으로 변환
        return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
    };

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

        const formattedCurrentTime = formatCurrentTime(new Date()); // 현재 시간을 포맷팅
        const formattedReceptionTime = formatReceptionTime(patient.receptionTime); // 접수 시간을 포맷팅

        console.log('포맷된 현재 시간:', formattedCurrentTime); // 포맷된 현재 시간 확인
        console.log('포맷된 접수 시간:', formattedReceptionTime); // 포맷된 접수 시간 확인

        // 새 행의 내용 설정
        row.innerHTML = `
        <td>${treatmentPatientCount}</td> <!-- 여기에 treatmentPatientCount 사용 -->
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${null || 'N/A'}</td>
        <td>${formattedCurrentTime || 'N/A'}</td> <!-- 포맷된 현재 시간 -->
        <td>${formattedReceptionTime || 'N/A'}</td> <!-- 포맷된 접수 시간 -->
    `;

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


    // 진료 완료 환자를 완료 테이블에 추가하는 함수
    function addPatientToCompleteTable(patient) {
        console.log('Adding patient to complete treatment table:', patient);

        const row = completePatientsTable.insertRow(); // 완료 테이블에 새 행 추가
        completePatientCount++; // 진료 완료 환자 수 증가

        // 현재 시간 포맷팅 함수
        const formatCurrentTime = (date) => {
            let hours = date.getHours();
            const minutes = date.getMinutes().toString().padStart(2, '0');
            const amPm = hours >= 12 ? '오후' : '오전';
            hours = hours % 12 || 12; // 12시간 형식으로 변환
            return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
        };

        // 접수 시간 포맷팅 함수
        const formatReceptionTime = (dateTimeString) => {
            if (!dateTimeString || dateTimeString === 'N/A') return 'N/A'; // 유효하지 않은 값 처리
            console.log('Input to formatReceptionTime:', dateTimeString);

            const date = new Date(dateTimeString); // ISO 형식을 Date 객체로 변환
            if (isNaN(date.getTime())) { // getTime()으로 유효성 검사
                console.error('Invalid dateTimeString:', dateTimeString);
                return 'N/A'; // 유효하지 않은 경우 N/A 반환
            }

            // UTC 시간으로 포맷팅 (오전/오후 표시)
            return date.toLocaleTimeString('ko-KR', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: true, // 오전/오후 표기
                timeZone: 'UTC', // UTC 시간대 유지
            });
        };

        const formattedViTime = formatCurrentTime(new Date()); // 진료 완료 시간을 현재 시간으로 포맷팅
        const formattedReceptionTime = formatReceptionTime(patient.receptionTime); // 접수 시간을 포맷팅

        row.innerHTML = `
        <td>${completePatientCount}</td> <!-- 완료 환자 수 사용 -->
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td> <!-- 포맷된 접수 시간 -->
        <td>${formattedViTime || 'N/A'}</td> <!-- 포맷된 진료 완료 시간 -->
    `;

        // 완료 환자 수 업데이트
        updateCompletePatientCount();
    }


    function updateWaitingPatientCount() {
        const count = waitingPatientsTable.rows.length;
        const header = document.querySelector("#waitingPatientsTable th[colspan='6']");
        header.textContent = `진료 대기 환자: ${count}명`;

    }

    function updateTreatmentPatientCount() {
        const count = treatmentPatientsTable.rows.length; // 현재 테이블의 행 개수
        const header = document.querySelector("#treatmentPatientsTable th[colspan='7']");

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


    // DB에서 환자 목록을 가져오고 테이블에 표시하는 함수
    function fetchAndDisplayPatients(selectedDate) {
        fetch("/api/patient-admission/waiting")
            .then(response => {
                if (!response.ok) {
                    throw new Error('환자 목록을 가져오는 데 실패했습니다.');
                }
                return response.json();
            })
            .then(data => {
                const currentPatients = Array.from(waitingPatientsTable.rows).map(row => ({
                    chartNum: row.cells[1].textContent,
                    rvTime: row.cells[4].textContent
                }));

                const filteredPatients = data.filter(patient => {
                    const rvTime = new Date(patient.rvTime);
                    return rvTime.toDateString() === selectedDate.toDateString();
                });

                filteredPatients.forEach(patient => {
                    if (!currentPatients.some(current => current.chartNum === patient.chartNum && new Date(current.rvTime).toDateString() === new Date(patient.rvTime).toDateString())) {
                        addPatientToWaitingTable(patient);
                    }
                });
            })
            .catch(error => {
                console.error("에러 발생:", error);
                alert("환자 목록을 가져오는 중 오류가 발생했습니다.");
            });
    }

    // 초기화: 페이지 로드 시 환자 목록을 가져옵니다.
    fetchAndDisplayPatients(today);

    // 매 5초마다 환자 목록 갱신
    setInterval(() => {
        const selectedDate = new Date(document.getElementById('currentDate').value);
        fetchAndDisplayPatients(selectedDate); // 현재 선택된 날짜의 환자 목록 갱신
    }, 5000);
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

    // 행 번호 업데이트 함수
    function updateRowIndexes() {
        const rows = waitingPatientsTable.getElementsByTagName("tr");
        for (let i = 0; i < rows.length; i++) {
            if (i > 0) { // 첫 번째 행은 헤더이므로 건너뜁니다.
                const cell = rows[i].cells[0]; // 첫 번째 열(번호 열)을 가져옵니다.
                cell.textContent = i; // 번호를 현재 행 번호로 업데이트합니다.
            }
        }
    }

    // 대기 환자 테이블의 행 클릭 시 해당 행 선택
    waitingPatientsTable.addEventListener("click", function (event) {
        const targetRow = event.target.closest("tr"); // 클릭한 셀의 행을 가져옴
        if (!targetRow || targetRow.rowIndex === 0) return; // 첫 번째 헤더는 무시

        // 선택된 행 저장
        selectedRow = targetRow;
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
            updateWaitingPatientCount(); // 환자 수 업데이트
            updateRowIndexes(); // 행 번호 업데이트
        }

        // 모달 닫기
        cancelModal.hide();
    });

    // '아니요' 버튼 클릭 시 모달 닫기
    closeModalBtn.addEventListener("click", function () {
        cancelModal.hide();
    });
});


