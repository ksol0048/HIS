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
                        return response.text().then(text => {
                            throw new Error(text || '환자 접수 실패');
                        });
                    }
                    return response.json(); // JSON으로 응답을 파싱
                })
                .then(data => {
                    console.log(data);

                    // 환자가 이미 대기 중 테이블에 존재하는지 확인
                    const existingRows = Array.from(waitingPatientsTable.rows);
                    const alreadyExists = existingRows.some(row => {
                        return row.cells[1].innerText === patientData.chartNum; // 차트 번호로 중복 검사
                    });

                    // 테이블에 추가되지 않았고 세션에 환자 정보가 없다면 추가
                    if (!alreadyExists) {
                        // 대기 중 테이블에 추가
                        addPatientToWaitingTable({
                            ...patientData,
                            receptionTime: new Date().toISOString(),
                            treatStatus: "1" // 대기 상태
                        });

                        // 세션 스토리지에 환자 정보 저장
                        let waitingPatients = JSON.parse(sessionStorage.getItem('waitingPatients')) || [];
                        waitingPatients.push(patientData);
                        sessionStorage.setItem('waitingPatients', JSON.stringify(waitingPatients));
                    } else {
                        alert("해당 환자는 이미 대기 중입니다.");
                    }
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


// 진료 시작 버튼 클릭 이벤트
    startTreatmentButton.addEventListener("click", function () {
        const selectedRow = waitingPatientsTable.querySelector('tr.selected');

        if (selectedRow) {
            const chartNum = selectedRow.cells[1].textContent;
            const paName = selectedRow.cells[2].textContent;
            const receptionTime = selectedRow.cells[5].textContent;

            // 선택된 행의 의사 정보를 가져옴
            const selectedDoctor = selectedRow.querySelector('select').value;
            console.log('선택된 의사:', selectedDoctor); // 의사 확인 로그 추가

            treatmentPatientInfo.textContent = `환자: ${paName}`;
            treatmentModal.show();

            const confirmTreatmentBtn = document.getElementById("confirmTreatmentBtn");
            confirmTreatmentBtn.onclick = function () {
                const formatReceptionTimeForDB = (timeString) => {
                    if (!timeString) return null;
                    const [amPm, time] = timeString.split(' ');
                    const [hours, minutes] = time.split(':').map(Number);
                    const hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

                    const now = new Date();
                    now.setHours(hoursIn24Format + 9, minutes); // 9시간 더하기
                    return now.toISOString();
                };

                const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime);

                // 세션 스토리지에서 진료 시작 시간 가져오기
                const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];
                const existingPatient = treatmentPatients.find(patient => patient.chartNum === chartNum);
                const treatmentStartTime = existingPatient ? existingPatient.treatmentStartTime : null;

                const patientData = {
                    chartNum: chartNum,
                    paName: paName,
                    treatStatus: "2",
                    receptionTime: formattedReceptionTimeForDB,
                    selectedDoctor: selectedDoctor, // 의사 정보
                    treatmentStartTime: treatmentStartTime || new Date().toISOString()
                };

                console.log('저장할 환자 데이터:', patientData);

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
                                throw new Error(err.message || '진료 시작 실패');
                            });
                        }

                        // 진료 중 환자 테이블에 추가
                        addPatientToTreatmentTable({
                            chartNum: chartNum,
                            paName: paName,
                            receptionTime: receptionTime,
                            selectedDoctor: selectedDoctor,
                            treatmentStartTime: treatmentStartTime
                        });

                        // 대기 중 환자 테이블에서 해당 환자 제거
                        const rowIndex = Array.from(waitingPatientsTable.rows).indexOf(selectedRow);
                        deletePatientFromWaitingTable(rowIndex);

                        // 대기 중 세션에서 해당 환자 제거
                        const waitingPatients = JSON.parse(sessionStorage.getItem('waitingPatients')) || [];
                        const updatedWaitingPatients = waitingPatients.filter(patient => patient.chartNum !== chartNum);
                        sessionStorage.setItem('waitingPatients', JSON.stringify(updatedWaitingPatients));

                        // 진료 중 세션에 환자 추가
                        const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];
                        treatmentPatients.forEach(patient => {
                            // 원하는 로직으로 선택된 의사 사용
                            console.log('세션에서 가져온 의사:', patient.selectedDoctor);
                        });


                        // 중복 확인 및 추가
                        const isPatientAlreadyInSession = treatmentPatients.some(patient => patient.chartNum === chartNum);
                        if (!isPatientAlreadyInSession) {
                            treatmentPatients.push(patientData);
                            sessionStorage.setItem('treatmentPatients', JSON.stringify(treatmentPatients)); // 세션에 저장
                        } else {
                            console.log("이미 진료 중인 환자입니다.");
                        }

                        const storedPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];
                        console.log('진료 중 세션에 저장된 환자 데이터:', storedPatients);

// 모든 환자 데이터를 순회하며 선택된 의사를 확인
                        storedPatients.forEach(patient => {
                            console.log(`차트 번호: ${patient.chartNum}, 선택된 의사: ${patient.selectedDoctor}`);
                        });

                        updateWaitingPatientCount();
                        alert("환자 진료가 시작되었습니다.");
                        treatmentModal.hide();
                    })
                    .catch(error => {
                        console.error("에러 발생:", error);
                        alert("진료 시작 중 오류가 발생했습니다: " + error.message);
                    });
            };
        } else {
            console.log("선택된 환자가 없습니다.");
            alert("진료를 시작할 환자를 선택해 주세요.");
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

    window.addEventListener('load', () => {
        const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];

        // 세션 스토리지에 저장된 모든 진료 중 환자 정보가 있는 경우 테이블에 추가
        treatmentPatients.forEach(patient => {
            const existingRows = Array.from(treatmentPatientsTable.rows);
            const alreadyExists = existingRows.some(row => {
                return row.cells[1].innerText === patient.chartNum; // 차트 번호로 중복 검사
            });

            // 환자 정보가 이미 진료 중이지 않으면 테이블에 추가
            if (!alreadyExists) {
                // 접수 시간에 15시간 추가
                const receptionTime = new Date(patient.receptionTime);
                receptionTime.setHours(receptionTime.getHours() + 15);
                const formattedReceptionTime = receptionTime.toISOString();

                // treatmentStartTime을 세션에서 가져온 값으로 유지
                const treatmentStartTime = patient.treatmentStartTime || 'N/A'; // 여기서 변하지 않도록 설정

                addPatientToTreatmentTable({
                    chartNum: patient.chartNum || 'N/A',
                    paName: patient.paName || 'N/A',
                    selectedDoctor: patient.selectedDoctor || 'N/A',
                    receptionTime: formattedReceptionTime, // 수정된 접수 시간
                    treatmentStartTime: treatmentStartTime, // 세션에서 가져온 진료 시작 시간
                    treatStatus: "2" // 진료 중 상태
                });
            }
        });

        // 진료 중 환자 수 업데이트
        updateTreatmentPatientCount();
    });


// 진료 완료 환자를 완료 테이블에 추가하는 함수
    function addPatientToCompleteTable(patient) {
        const completePatientsTable = document.getElementById('completedPatientsTable'); // 테이블 ID를 통해 테이블 요소를 가져옴
        if (!completePatientsTable) {
            console.error("Completed Patients Table not found!");
            return;
        }

        const row = completePatientsTable.insertRow(); // 완료 테이블에 새 행 추가
        completePatientCount++; // 진료 완료 환자 수 증가
        console.log("Patient's reception time:", patient.receptionTime);

        // 현재 시간 포맷팅 함수
        const formatCurrentTime = (date) => {
            let hours = date.getHours();
            const minutes = date.getMinutes().toString().padStart(2, '0');
            const amPm = hours >= 12 ? '오후' : '오전';
            hours = hours % 12 || 12; // 12시간 형식으로 변환
            return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
        };

        // 접수 시간 포맷팅 함수 (UTC+6으로 변환)
        const formatReceptionTime = (dateTimeString) => {
            if (!dateTimeString || dateTimeString === 'N/A') return 'N/A'; // 유효하지 않은 값 처리
            const date = new Date(dateTimeString); // ISO 형식을 Date 객체로 변환
            const utcOffset = 15 * 60; // UTC+6
            const convertedDate = new Date(date.getTime() + utcOffset * 60 * 1000); // UTC+6으로 변환

            let hours = convertedDate.getHours();
            const minutes = convertedDate.getMinutes().toString().padStart(2, '0');
            const amPm = hours >= 12 ? '오후' : '오전';
            hours = hours % 12 || 12; // 12시간 형식으로 변환
            return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
        };

        // 진료 완료 시간 포맷팅 함수 (cpTime으로 변경)
        const formatCpTime = (dateTimeString) => {
            if (!dateTimeString || dateTimeString === 'N/A') return 'N/A'; // 유효하지 않은 값 처리
            const date = new Date(dateTimeString); // ISO 형식을 Date 객체로 변환
            return date.toLocaleString('ko-KR', {hour: '2-digit', minute: '2-digit', hour12: true}); // 진료 완료 시간 포맷팅
        };

        const formattedReceptionTime = formatReceptionTime(patient.receptionTime); // 접수 시간을 포맷
        const formattedCpTime = formatCurrentTime(new Date());

        row.innerHTML = `
        <td>${completePatientCount}</td>
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td>
        <td>${formattedCpTime || 'N/A'}</td> <!-- 포맷된 진료 완료 시간 -->
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


