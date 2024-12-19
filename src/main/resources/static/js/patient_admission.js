const waitingPatientsTable = document.getElementById("waitingPatientsTable").getElementsByTagName("tbody")[0];
const treatmentPatientsTable = document.getElementById("treatmentPatientsTable").getElementsByTagName("tbody")[0];
const completePatientsTable = document.getElementById("completedPatientsTable").getElementsByTagName("tbody")[0];
const currentDate = document.getElementById("currentDate");
const receptionBtn = document.querySelector(".ReceptionBtn");
const startTreatmentButton = document.getElementById("startTreatmentButton");
const treatmentPatientInfo = document.getElementById("treatmentPatientInfo");
const completeTreatmentButton = document.getElementById("completeTreatmentButton");
const treatmentModalElement = document.getElementById('treatmentModal');
const confirmCompleteButton = document.getElementById("confirmCompleteButton");
const completedPatientsTable = document.getElementById('completedPatientsTable');
const completedPatientsBody = completedPatientsTable.querySelector('tbody');


const waitingPatients = [];
const treatmentPatients = [];
const completePatients = [];


function loadWaiting() {

    console.log("페이지 로드 중: 대기 환자 목록을 불러오는 중...");

    fetch('/api/patient-admission/waiting')
        .then(response => {
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log("대기 환자 목록을 성공적으로 가져왔습니다:", data);
            data.forEach(patient => {
                // 대기 환자 목록에 추가
                waitingPatients.push(patient);
                addPatientToWaitingTable(patient); // 테이블에 환자 목록 추가
            });
        })
        .catch(error => {
            console.error("대기 환자 목록을 불러오는 중 오류 발생:", error);
        });

}

function loadTreatment() {

    console.log("페이지 로드 중: 진료 중 환자 목록을 불러오는 중...");

    fetch('/api/patient-admission/treatment')
        .then(response => {
            console.log("응답 상태:", response.status);
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log("진료 중 환자 목록을 성공적으로 가져왔습니다:", data);
            data.forEach(patient => {
                treatmentPatients.push(patient); // 진료 중 환자 목록에 추가
                addPatientToTreatmentTable(patient); // 테이블에 환자 목록 추가
                // console.log("진료 중 환자 정보:", patient);
            });
        })
        .catch(error => {
            console.error("진료 중 환자 목록을 불러오는 중 오류 발생:", error);
        });

}

function loadComplete() {

    console.log("페이지 로드 중: 진료 중 환자 목록을 불러오는 중...");

    fetch('/api/patient-admission/complete')
        .then(response => {
            console.log("응답 상태:", response.status);
            if (!response.ok) {
                throw new Error('서버 응답 오류: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log("진료 중 환자 목록을 성공적으로 가져왔습니다:", data);
            data.forEach(patient => {
                treatmentPatients.push(patient); // 진료 중 환자 목록에 추가
                addPatientToTreatmentTable(patient); // 테이블에 환자 목록 추가
                // console.log("진료 중 환자 정보:", patient);
            });
        })
        .catch(error => {
            console.error("진료 중 환자 목록을 불러오는 중 오류 발생:", error);
        });

}

function callPatientStatus() {
    fetch("/api/patient-admission/status")
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            // 초기 환자 상태 업데이트
            if (data) {
                document.getElementById('home-waitingCount').textContent = data.status1;
                document.getElementById('home-inTreatmentCount').textContent = data.status2;
                document.getElementById('home-completedCount').textContent = data.status3;
            }
        })
        .catch(error => console.error('Error fetching patient status:', error));
}


let waitingPatientCount = 0;
let treatmentPatientCount = 0;
let completePatientCount = 0;

document.addEventListener("DOMContentLoaded", function () {

    // enableDragToTreatmentWithSelection(waitingPatientsTable, treatmentPatientsTable);
    // 오늘 날짜 기본값 설정 (한국 시간으로)
    const today = new Date();

    // 한국 시간대(UTC+9)로 변환
    const koreaOffset = 9 * 60; // 한국은 UTC+9
    today.setMinutes(today.getMinutes() + today.getTimezoneOffset() + koreaOffset); // 한국 시간대로 맞추기

    // 자정으로 설정
    today.setHours(0, 0, 0, 0); // 자정으로 설정

    // 한국 시간대에서 자정으로 설정된 날짜 가져오기
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(today.getDate()).padStart(2, '0');

    const todayFormattedDate = `${year}-${month}-${day}`; // YYYY-MM-DD 형식으로 변환

    // 확인용 콘솔 로그
    console.log("오늘 날짜 (한국 시간, 자정):", todayFormattedDate);

    // 날짜 입력창에 값 설정
    currentDate.value = todayFormattedDate;
    callPatientListRender();
    callDate();
    loadWaiting();
    loadTreatment();
    loadComplete();
    cancelReception();
    updateRowNumbers();


    updateWaitingPatientCount();
    updateTreatmentPatientCount();
    updateCompletePatientCount();

    // WebSocket 연결 및 대기 환자 목록 수신
    stompClient.connect({}, function (frame) {
        console.log('WebSocket 연결 성공: ' + frame);

        stompClient.subscribe('/topic/waitingPatients', function (message) {
            console.log("수신된 메시지:", message.body);
            const Patient = JSON.parse(message.body);

            // 환자 객체가 null인지 확인
            if (!Patient) {
                console.error("수신된 환자 데이터가 없습니다:", message.body);
                return; // 환자 데이터가 없으면 처리하지 않음
            }

            console.log("환자 ID (pid):", Patient.pid); // 환자 ID 로그 출력

            if (!Patient.pid) {
                console.error("환자 ID (pid)가 없습니다. 수신된 데이터:", Patient);
                return; // pid가 없으면 처리하지 않음
            }

            // 대기 환자 목록에 중복된 환자가 있는지 확인
            const isDuplicate = waitingPatients.some(existingPatient => existingPatient.pid === Patient.pid);

            if (isDuplicate) {
                console.log("이미 대기 목록에 존재하는 환자입니다. 추가하지 않습니다:", Patient);
                return; // 중복 환자는 추가하지 않음
            }

            // 대기 환자 목록에 바로 추가
            waitingPatients.push(Patient);
            console.log("대기 환자 목록에 추가된 환자:", Patient);

            // 대기 테이블에 환자 추가
            addPatientToWaitingTable(Patient);
        });


        // 진료 중 환자 목록을 받기 위한 구독
        stompClient.subscribe('/topic/inTreatmentPatients', function (message) {
            const newPatient = JSON.parse(message.body);
            console.log('진료 중 환자 수신:', newPatient);  // 추가된 로그

            // 중복 환자 체크 후 추가
            const isPatientAlreadyAdded = treatmentPatients.some(patient => patient.pid === newPatient.pid);
            if (!isPatientAlreadyAdded) {
                treatmentPatients.push(newPatient);
                addPatientToTreatmentTable(newPatient);
                console.log(`새 환자 추가: ${newPatient.paName} (PID: ${newPatient.pid})`);
            } else {
                console.log(`중복된 환자 제외: ${newPatient.paName} (PID: ${newPatient.pid})`);
            }
        });

        // 진료 완료 환자 목록을 받기 위한 구독
        stompClient.subscribe('/topic/inCompletePatients', function (message) {
            const newPatient = JSON.parse(message.body);
            console.log('진료 완료 환자 수신:', newPatient);  // 추가된 로그

            // 중복 환자 체크 후 추가
            const isPatientAlreadyAdded = completePatients.some(patient => patient.pid === newPatient.pid);
            if (!isPatientAlreadyAdded) {
                completePatients.push(newPatient);
                addPatientToCompleteTable(newPatient);
                console.log(`새 환자 추가: ${newPatient.paName} (PID: ${newPatient.pid})`);
            } else {
                console.log(`중복된 환자 제외: ${newPatient.paName} (PID: ${newPatient.pid})`);
            }
        });
    });
    // function enableDragToTreatmentWithSelection(waitingPatientsTable, treatmentPatientsTable) {
    //     // 대기 테이블 행 클릭 시 선택 설정
    //     Array.from(waitingPatientsTable.rows).forEach(row => {
    //         row.addEventListener('click', () => {
    //             const previouslySelected = waitingPatientsTable.querySelector('tr.selected');
    //             if (previouslySelected) {
    //                 previouslySelected.classList.remove('selected'); // 기존 선택 해제
    //             }
    //             row.classList.add('selected'); // 현재 행 선택
    //         });
    //
    //         // 드래그 가능 설정
    //         row.setAttribute('draggable', 'true');
    //
    //         // 드래그 시작 이벤트
    //         row.addEventListener('dragstart', (event) => {
    //             const isSelected = row.classList.contains('selected'); // 선택 여부 확인
    //             if (!isSelected) {
    //                 event.preventDefault(); // 선택되지 않은 행은 드래그 불가
    //                 alert("행을 선택한 후 드래그하세요."); // 경고 메시지
    //                 return;
    //             }
    //             const pid = row.querySelector('td:nth-child(7)').textContent; // PID 가져오기
    //             event.dataTransfer.setData('text/plain', pid); // PID를 데이터로 저장
    //             row.classList.add('dragging'); // 드래그 표시
    //         });
    //
    //         // 드래그 종료 이벤트
    //         row.addEventListener('dragend', () => {
    //             row.classList.remove('dragging');
    //         });
    //     });
    //
    //     // 진료 중 테이블 드롭 이벤트
    //     treatmentPatientsTable.addEventListener('dragover', (event) => {
    //         event.preventDefault(); // 기본 동작 방지
    //         treatmentPatientsTable.classList.add('dragover');
    //     });
    //
    //     treatmentPatientsTable.addEventListener('dragleave', () => {
    //         treatmentPatientsTable.classList.remove('dragover');
    //     });
    //
    //     treatmentPatientsTable.addEventListener('drop', (event) => {
    //         event.preventDefault();
    //         treatmentPatientsTable.classList.remove('dragover');
    //
    //         const draggedPid = event.dataTransfer.getData('text/plain'); // 드래그된 PID
    //         const draggedRow = Array.from(waitingPatientsTable.rows).find(row =>
    //             row.querySelector('td:nth-child(7)')?.textContent === draggedPid
    //         );
    //
    //         if (draggedRow) {
    //             // 선택된 행을 기존 버튼 로직을 통해 진료 중 테이블로 이동
    //             const startTreatmentButton = document.getElementById('startTreatmentButton');
    //             startTreatmentButton.click(); // 버튼 클릭 이벤트 호출
    //         }
    //     });
    // }



    receptionBtn.addEventListener("click", function () {
        console.log("접수 버튼이 클릭되었습니다.");

        // 권한 체크
        const hasPermission = globalUserData.authorities.some(auth =>
            auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
        );

        if (!hasPermission) {
            alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
            return;
        }

        // 세션에서 환자 데이터 가져오기
        const selectedPatient = JSON.parse(sessionStorage.getItem('selectedPatient'));

        if (selectedPatient) {
            console.log("가져온 환자 정보:", selectedPatient);

            // 서버로 보낼 데이터 생성
            const patientData = {
                chartNum: selectedPatient.chartNum,
                paName: selectedPatient.name || "N/A",
                mainDoc: null,
                rvTime: null,
                receptionTime: new Date().toISOString(),
            };

            fetch("/api/patient-admission/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(patientData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => {
                            throw new Error(text || '환자 접수 실패');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    console.log("서버 응답:", data);

                    if (data && data.data && data.data.pid) {
                        patientData.pid = data.data.pid;

                        // 테이블에 추가
                        addPatientToWaitingTable(patientData);

                        // WebSocket으로 알림 전송
                        stompClient.send("/topic/waitingPatients", {}, JSON.stringify(patientData));

                        alert("환자 접수가 완료되었습니다.");
                    } else {
                        console.error("서버 응답에 PID가 포함되지 않았습니다.");
                    }
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("환자 접수 중 문제가 발생했습니다.");
                });
        } else {
            alert("선택된 환자가 없습니다.");
        }
    });


    waitingPatientsTable.addEventListener('click', (event) => {
        const targetRow = event.target.closest('tr');
        if (targetRow) {
            console.log("클릭된 행:", targetRow);
            const previouslySelected = waitingPatientsTable.querySelector('tr.selected');
            if (previouslySelected) {
                previouslySelected.classList.remove('selected');
            }
            targetRow.classList.add('selected');
            console.log("현재 선택된 행:", targetRow);
        }
    });

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
        const selectedRow = waitingPatientsTable.querySelector('tr.selected');
        console.log("startTreatmentButton->", selectedRow)

        // 권한 체크를 직접 수행합니다.
        const hasPermission = globalUserData.authorities.some(auth =>
            auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
        );

        // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
        if (!hasPermission) {
            alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
            return; // 등록 과정 중단
        }

        if (!selectedRow) {
            alert("선택된 환자가 없습니다. 환자를 선택한 후 다시 시도하세요.");
            return; // 등록 과정 중단
        }
        console.log("선택된 환자의 데이터:", selectedRow.cells);

        console.log("Waiting Patients Table:", waitingPatientsTable.rows);

        // 선택된 환자의 정보를 가져옵니다.
        const chartNum = selectedRow.cells[1].textContent;
        const paName = selectedRow.cells[2].textContent;
        const receptionTime = selectedRow.cells[5].textContent;
        // 선택된 환자의 pid를 가져옵니다.
        const pid = selectedRow.cells[6].textContent; // 여기서 pid가 포함된 열의 인덱스를 조정하세요.

        const selectedDoctor = selectedRow.querySelector('select').value;

        treatmentPatientInfo.textContent = `환자: ${paName}`;
        const treatmentModal = new bootstrap.Modal(document.getElementById('treatmentModal'));
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
            }

            const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime);
            const treatmentPatients = JSON.parse(sessionStorage.getItem('treatmentPatients')) || [];
            const existingPatient = treatmentPatients.find(patient => patient.chartNum === chartNum);
            let viTime = existingPatient ? existingPatient.viTime : null; // viTime 수정

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
                viTime: viTime || new Date().toISOString(), // viTime 수정
                pid: pid // 여기서 pid 추가
            };

            console.log("디비에 저장되는 viTime", viTime);
            console.log('저장할 환자 데이터:', patientData);
            console.log(`환자 정보 - 차트 번호: ${chartNum}, 이름: ${paName}, 접수 시간: ${receptionTime}, 의사: ${selectedDoctor}, PID: ${pid}`);

            fetch("/api/patient-admission/treatment/start", {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(patientData)

            })
                .then(response => {
                    console.log("------------>", response)
                    if (!response.ok) {
                        return response.json().then(err => {
                            throw new Error(err.message || '진료 시작 실패');
                        });
                    }
                    callPatientListRender();
                    const rowIndex = Array.from(waitingPatientsTable.rows).indexOf(selectedRow);
                    deletePatientFromWaitingTable(rowIndex);
                    treatmentModal.hide();
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("진료 시작 중 오류가 발생했습니다: " + error.message);
                });
            updateTreatmentPatientCount();
        }
    });


    // treatmentModalElement.addEventListener('hidden.bs.modal', function () {
    //     treatmentPatientInfo.textContent = '';
    // });

    // 진료 완료 버튼 클릭 시 모달 표시
    completeTreatmentButton.addEventListener("click", function () {
        // 권한 체크를 직접 수행합니다.
        const hasPermission = globalUserData.authorities.some(auth =>
            auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
        );

        // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
        if (!hasPermission) {
            alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
            return; // 등록 과정 중단
        }

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

        const chartNum = selectedRow.cells[1].textContent;
        const paName = selectedRow.cells[2].textContent;
        const selectedDoctor = selectedRow.cells[3].textContent;
        const receptionTime = selectedRow.cells[4].textContent;
        const viTime = selectedRow.cells[5].textContent;
        const rvTime = selectedRow.cells[6] ? selectedRow.cells[6].textContent : null; // rvTime이 포함된 열의 인덱스 조정 필요

        // viTime 값 변환
        const formattedViTimeForDB = formatViTimeForDB(viTime);
        console.log("변환된 viTime 값:", formattedViTimeForDB);

        // receptionTime 변환
        const formattedReceptionTimeForDB = formatReceptionTimeForDB(receptionTime);
        console.log("변환된 receptionTime 값:", formattedReceptionTimeForDB); // 변환된 receptionTime 로그 출력

        const formattedRvTimeForDB = rvTime ? formatRvTimeForDB(rvTime) : null; // rvTime 변환 함수 필요
        console.log("변환된 rvTime 값:", formattedRvTimeForDB);

        // 환자 이름과 확인 메시지 설정
        const completeInfo = document.getElementById('completeInfo');
        completeInfo.textContent = `환자: ${paName}`; // 환자 이름과 함께 메시지 표시

        // 모달 보이기
        const completeModal = new bootstrap.Modal(document.getElementById('completeModal'));
        completeModal.show();

        confirmCompleteButton.onclick = function () {
            // 현재 시간으로 completionTime 생성 (UTC+9로 조정)
            const completionTime = new Date(); // 현재 시간 가져오기
            const completionTimeKST = new Date(completionTime.getTime() + (9 * 60 * 60 * 1000));

            const formattedCompletionTimeValue = formatCompleteTime(completionTime);
            console.log("진료 완료 시간:", formattedCompletionTimeValue);

            // DB에 저장할 데이터 준비
            const dataToSend = {
                chartNum,
                paName,
                treatStatus: "3", // 진료 완료 상태
                mainDoc: selectedDoctor,
                receptionTime: formattedReceptionTimeForDB, // 변환된 receptionTime 사용
                viTime: formattedViTimeForDB, // viTime을 여기에서 사용
                rvTime: formattedRvTimeForDB,
                completionTime: completionTime.toISOString(), // 추가된 진료 완료 시간
            };

            console.log("저장할 데이터:", dataToSend);

            // API 호출
            fetch("/api/patient-admission/completeTreatment", {
                method: "PUT",
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

                    // 진료 중 환자 테이블에서 해당 환자 제거
                    const rowIndex = Array.from(treatmentPatientsTable.rows).indexOf(selectedRow);
                    deletePatientFromTreatmentTable(rowIndex);
                    callPatientListRender();
                    completeModal.hide();
                })
                .catch(error => {
                    console.error("에러 발생:", error);
                    alert("진료 완료 처리 중 오류가 발생했습니다: " + error.message);
                });
        }
        updateCompletePatientCount();

    });



    const formatViTimeForDB = (timeString) => {
        if (!timeString) return null;

        const timePattern = /^(오전|오후) (\d{1,2}):(\d{1,2})$/;
        const match = timeString.match(timePattern);
        if (!match) {
            console.error(`시간 형식이 잘못되었습니다: ${timeString}`);
            return null;
        }

        const amPm = match[1];
        const hours = parseInt(match[2], 10);
        const minutes = parseInt(match[3], 10);
        let hoursIn24Format = (amPm === '오후' && hours !== 12) ? hours + 12 : (amPm === '오전' && hours === 12) ? 0 : hours;

        const now = new Date();
        now.setHours(hoursIn24Format, minutes, 0, 0);

        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        const hour = String(now.getHours()).padStart(2, '0');
        const minute = String(now.getMinutes()).padStart(2, '0');

        return `${year}-${month}-${day}T${hour}:${minute}:00`; // 'T' 추가
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


});


function formatCompleteTime(completionTime) {
    console.log("입력된 진료 완료 시간: ", completionTime);
    if (!completionTime || completionTime === 'N/A') {
        return 'N/A';
    }

    const date = new Date(completionTime);
    if (isNaN(date.getTime())) {
        return 'N/A';
    }

    // UTC+9 적용
    const utcOffset = 9 * 60 * 60 * 1000; // 9시간을 밀리초로 변환
    const localDate = new Date(date.getTime() + utcOffset);

    let hours = localDate.getHours();
    const minutes = String(localDate.getMinutes()).padStart(2, '0');
    const amPm = hours >= 12 ? '오후' : '오전';
    hours = hours % 12 || 12;

    return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`;
}
function formatRvTimeForDB(rvTime) {
    const date = new Date(rvTime);
    if (isNaN(date.getTime())) {
        return null;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day}T${hours}:${minutes}:00`; // 'T' 추가
}


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
        updateRowNumbers();
        updateWaitingPatientCount();
    } else {
        console.error(`Invalid row index: ${rowIndex}`);
    }
}


// 행 번호 업데이트
function updateRowNumbers() {
    const rows = waitingPatientsTable.rows;
    for (let i = 0; i < rows.length; i++) {
        rows[i].cells[0].innerText = String(i + 1);
    }
}

function formatRvTime(rvTime) {
    if (!rvTime) return 'N/A';

    const date = new Date(rvTime);
    return date.toLocaleString([], {
        hour: '2-digit',
        minute: '2-digit',
        hour12: true // 오전/오후 형식
    });
}


// 대기 환자 테이블 클릭 시
waitingPatientsTable.addEventListener("click", function (event) {
    const targetRow = event.target.closest("tr"); // 클릭한 셀의 행을 가져옴
    if (!targetRow) return;

    // 이전에 선택된 행의 선택 클래스를 제거
    const previousSelectedRow = document.querySelector('.clicked');
    if (previousSelectedRow && previousSelectedRow !== targetRow) {
        previousSelectedRow.classList.remove("clicked");
    }

    // 클릭한 행에 클릭 클래스를 추가
    targetRow.classList.add("clicked");

    // data-pid가 설정되지 않았다면, 새로 설정
    let pid = targetRow.getAttribute('data-pid');
    if (!pid) {
        pid = targetRow.cells[6]?.textContent?.trim();  // PID가 없으면 셀에서 가져옴
        if (pid) {
            targetRow.setAttribute('data-pid', pid); // PID를 data-pid에 설정
        }
    }

    // 세션에서 PID 값을 바로 읽기
    const pidFromSession = sessionStorage.getItem('patientPID');
    if (pidFromSession) {
        console.log("세션에서 읽은 PID:", pidFromSession); // 바로 읽은 PID 값
        // 여기서 즉시 DOM에 표시하거나 필요한 작업을 진행할 수 있습니다.

        // 예: PID 값을 DOM에 표시
        const pidValueElement = document.getElementById("pidValue");
        if (pidValueElement) {
            pidValueElement.textContent = pidFromSession; // PID 값을 표시
        }
    } else {
        console.warn("세션에 저장된 PID가 없습니다.");
    }

    // PID 값을 세션에 저장
    if (pid) {
        sessionStorage.setItem('selectedPID', pid); // 세션에 PID 저장
        console.log("선택된 PID:", pid); // PID 확인용 로그
    } else {
        console.warn("PID를 찾을 수 없습니다. 선택된 행:", targetRow);
    }
});



function addPatientToWaitingTable(patient) {
    const existingRows = waitingPatientsTable.getElementsByTagName('tr');
    const isDuplicate = Array.from(existingRows).some(row => {
        const pidCell = row.querySelector('td:nth-child(7)');
        return pidCell && pidCell.textContent === patient.pid.toString();
    });

    if (isDuplicate) {
        return; // 중복된 환자는 추가하지 않음
    }

    const row = waitingPatientsTable.insertRow();
    row.setAttribute('data-pid', patient.pid); // PID 속성 추가
    const currentRowCount = existingRows.length + 1;
    const pid = row.getAttribute('data-pid');
    if (pid) {
        sessionStorage.setItem('selectedPID', pid); // 세션에 PID 저장
        console.log("새로 추가된 PID:", pid);
    } else {
        console.warn("PID를 찾을 수 없습니다. 추가된 행:", row);
    }
    row.innerHTML = `
        <td>${currentRowCount}</td>
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>
            <select>
                ${doctorNames.map(name => `<option value="${name}">${name}</option>`).join('')}
            </select>
        </td>
        <td>${formatRvTime(patient.rvTime) || ''}</td>
        <td>${patient.receptionTime ? new Date(patient.receptionTime).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
    }) : 'N/A'}</td>
        <td style="display: none;">${patient.pid}</td>
    `;

    // PID 확인
    const pidCheck = row.getAttribute('data-pid');
    if (!pidCheck) {
        console.warn("PID 설정 실패! 행:", row);
    } else {
        console.log("PID 설정 성공! PID:", pidCheck);
    }

    // 세션 스토리지에 PID 저장
    sessionStorage.setItem('currentPID', patient.pid);  // 환자 PID 저장
    console.log("세션에 저장된 PID:", patient.pid);

    // 로그 확인
    console.log("새로 추가된 행:", row);
    console.log("새로 추가된 PID:", row.getAttribute('data-pid')); // PID 설정 확인

    updateWaitingPatientCount(); // 대기 환자 수 업데이트
    updateRowNumbers(); // 행 번호 업데이트
}



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
    if (!dateTimeString || dateTimeString === 'N/A') return 'N/A';

    // ISO 형식인지 확인
    if (dateTimeString.includes('T')) {
        const date = new Date(dateTimeString);
        return formatCurrentTime(date); // ISO 형식일 때 포맷팅하여 반환
    }

    // ISO 형식이 아닌 경우, 기존 로직 사용
    const parts = dateTimeString.split(' ');
    if (parts.length !== 2) {
        console.error('시간 형식이 잘못되었습니다:', dateTimeString);
        return 'N/A'; // 형식 오류 처리
    }

    const [period, time] = parts;
    console.log('오전/오후:', period, '시간:', time);

    // 시간과 분 파싱
    let [hours, minutes] = time.split(':').map(Number);
    console.log('파싱된 시간:', hours, '파싱된 분:', minutes);

    // 유효성 검사
    if (isNaN(hours) || isNaN(minutes)) {
        console.error('유효하지 않은 시간 또는 분:', hours, minutes);
        return 'N/A';
    }

    if (period === '오후' && hours < 12) {
        hours += 12;
    } else if (period === '오전' && hours === 12) {
        hours = 0;
    }

    // 날짜 객체 생성
    const date = new Date();
    date.setHours(hours, minutes);

    const formattedTime = formatCurrentTime(date);

    return formattedTime; // 포맷된 시간 반환
};


function addPatientToTreatmentTable(patient) {
    // console.log('진료 중 환자 테이블에 추가하는 환자:', patient); // 데이터 확인

    // 중복 체크: pid로 기존 환자 확인
    const isPatientAlreadyAdded = treatmentPatients.some(p => p.pid === patient.pid);
    if (isPatientAlreadyAdded) {
        console.log(`중복된 환자 제외: ${patient.paName} (PID: ${patient.pid})`);
        return; // 중복된 환자라면 함수를 종료
    }

    const row = treatmentPatientsTable.insertRow(); // 진료 중 테이블에 새 행 추가
    treatmentPatientCount++; // 진료 중 환자 수 증가

    const formattedReceptionTime = formatReceptionTime(patient.receptionTime); // 접수 시간을 포맷팅
    const treatmentStartTime = patient.viTime
        ? formatCurrentTime(new Date(new Date(patient.viTime).setHours(new Date(patient.viTime).getHours() + 15)))
        : formatCurrentTime(new Date()); // 진료 시작 시간 포맷팅

    // 새 행의 내용 설정
    row.innerHTML = `
        <td>${treatmentPatientCount}</td> <!-- 여기에 treatmentPatientCount 사용 -->
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td> <!-- 포맷된 접수 시간 -->
        <td>${treatmentStartTime || 'N/A'}</td> <!-- 포맷된 진료 시작 시간 -->
        <td style="display: none;">${patient.pid}</td>
    `;

    // 행 선택 기능 추가
    row.addEventListener('click', () => {
        const previouslySelected = treatmentPatientsTable.querySelector('tr.selected');
        if (previouslySelected) {
            previouslySelected.classList.remove('selected'); // 이전 선택된 행에서 클래스를 제거
        }
        row.classList.add('selected'); // 현재 클릭된 행에 클래스를 추가
    });
    updateTreatmentPatientCount();
    // 진료 환자 목록에 추가
    treatmentPatients.push(patient); // 환자를 목록에 추가

}


// 진료 완료 환자를 완료 테이블에 추가하는 함수
function addPatientToCompleteTable(patient) {
    const completePatientsTable = document.getElementById('completedPatientsTable');
    if (!completePatientsTable) {
        return;
    }

    const tbody = completePatientsTable.getElementsByTagName('tbody')[0];
    if (!tbody) {
        return;
    }

    const chartNum = patient.chartNum || 'N/A';
    let isDuplicate = false;

    for (let i = 0; i < tbody.rows.length; i++) {
        const cells = tbody.rows[i].cells;
        const existingChartNum = cells[1].innerText; // 두 번째 열이 차트 번호
        if (existingChartNum === chartNum) {
            isDuplicate = true; // 중복된 차트 번호 발견
            break; // 반복문 종료
        }
    }

    if (isDuplicate) {
        console.log(`차트 번호 ${chartNum}는 이미 존재합니다. 행 추가를 건너뜁니다.`);
        return; // 중복된 경우 함수 종료
    }

    completePatientCount++;
    // 접수 시간 UTC+6으로 변환
    const formatReceptionTime = (dateTimeString) => {
        if (!dateTimeString || dateTimeString === 'N/A') return 'N/A';
        const date = new Date(dateTimeString);
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

        // completionTime이 12시간 형식일 경우 처리
        if (typeof completionTime === 'string' && /[오전|오후]/.test(completionTime)) {
            const [amPm, timePart] = completionTime.split(" "); // 오전/오후와 시간 부분을 분리
            const [hoursStr, minutesStr] = timePart.split(":"); // 시와 분 분리

            const hours = parseInt(hoursStr, 10); // 문자열을 정수로 변환
            const minutes = parseInt(minutesStr, 10); // 문자열을 정수로 변환

            // 12시간 형식을 24시간 형식으로 변환
            let convertedHours = amPm === '오후' && hours < 12 ? hours + 12 : hours;
            convertedHours = amPm === '오전' && hours === 12 ? 0 : convertedHours; // 12 AM은 0시로 변환

            const date = new Date();
            date.setHours(convertedHours);
            date.setMinutes(minutes);
            date.setSeconds(0);

            // UTC+6 시간으로 변환
            const utcOffset = -6 * 60 * 60 * 1000; // 6시간을 밀리초로 변환
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
        const utcOffset = -9 * 60 * 60 * 1000; // 6시간을 밀리초로 변환
        const localDate = new Date(date.getTime() + utcOffset); // UTC 시간에 오프셋 추가

        let hours = localDate.getHours(); // 지역 시간의 시를 가져옴
        const minutes = localDate.getMinutes().toString().padStart(2, '0'); // 분을 2자리로 포맷팅
        const amPm = hours >= 12 ? '오후' : '오전'; // 오전/오후 구분
        hours = hours % 12 || 12; // 12시간 형식으로 변환

        return `${amPm} ${hours.toString().padStart(2, '0')}:${minutes}`; // 포맷팅된 시간 반환
    };


    const formattedCpTime = formatCompleteTime(patient.completionTime);

    const formattedReceptionTime = formatReceptionTime(patient.receptionTime);

    // tbody에 새로운 행 추가
    const row = tbody.insertRow(); // tbody에서 행을 추가
    row.innerHTML = `
        <td>${completePatientCount}</td>
        <td>${patient.chartNum || 'N/A'}</td>
        <td>${patient.paName || 'N/A'}</td>
        <td>${patient.selectedDoctor || 'N/A'}</td>
        <td>${formattedReceptionTime || 'N/A'}</td>
        <td>${formattedCpTime || 'N/A'}</td>
        <td style="display: none;">${patient.pid}</td>
    `;

    for (let i = 0; i < tbody.rows.length; i++) {
        const cells = tbody.rows[i].cells;
        const rowData = Array.from(cells).map(cell => cell.innerText).join(', ');
        console.log(`행 ${i + 1}: ${rowData}`);
    }
    updateRowNumbers();
    // 완료 환자 수 업데이트
    updateCompletePatientCount();
}

async function fetchPatientStatus() {
    try {
        const response = await fetch('/api/patient-admission/status'); // 서버의 실제 경로
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json(); // JSON 데이터를 파싱
        console.log("9999999999999999", data)

        return data; // 파싱된 데이터를 반환
    } catch (error) {
        console.error("Error fetching patient status:", error);
        return { status1: 0, status2: 0, status3: 0 }; // 오류 발생 시 기본값 반환
    }
}


async function updateWaitingPatientCount() {
    const data = await fetchPatientStatus();
    const status1Count = data.status1; // 진료 대기 환자 수
    const header = document.querySelector("#waitingPatientsTable th[colspan='6']");
    header.textContent = `진료 대기 환자: ${status1Count}명`;
}

async function updateTreatmentPatientCount() {
    const data = await fetchPatientStatus();
    const status2Count = data.status2; // 진료 중 환자 수
    const header = document.querySelector("#treatmentPatientsTable th[colspan='6']");
    header.textContent = `진료 중 환자: ${status2Count}명`;
}

async function updateCompletePatientCount() {
    const data = await fetchPatientStatus();
    const status3Count = data.status3; // 진료 완료 환자 수
    const header = document.querySelector('#completedPatientsTable th[colspan="6"]');
    header.textContent = `진료 완료 환자: ${status3Count}명`;
}



function cancelReception() {
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
    });

    // 접수 취소 버튼 클릭 시 모달 표시
    cancelReceptionButton.addEventListener("click", function () {
        // 권한 체크를 직접 수행합니다.
        const hasPermission = globalUserData.authorities.some(auth =>
            auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
        );

        // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
        if (!hasPermission) {
            alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
            return; // 등록 과정 중단
        }

        if (selectedRow) {
            const patientName = selectedRow.cells[2]?.textContent || "";
            patientNameElement.textContent = `환자 이름: ${patientName}`;
            cancelModal.show();
        } else {
            alert("취소할 환자를 먼저 선택하세요.");
        }
    });

    confirmCancelBtn.addEventListener("click", function () {
        if (!selectedRow) {
            alert("선택된 행이 없습니다. 다시 시도하세요.");
            return;
        }

        // 세션에서 PID 가져오기
        const pid = sessionStorage.getItem('selectedPID');
        if (!pid) {
            alert("PID 정보를 찾을 수 없습니다. 다시 시도하세요.");
            return;
        }

        // 서버에 접수 취소 요청
        fetch(`/api/patient-admission/${pid}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    selectedRow.remove(); // 선택된 행 삭제
                    selectedRow = null; // 초기화
                    updateRowIndexes(); // 행 번호 업데이트
                    updateWaitingPatientCount(); // 환자 수 업데이트
                } else {
                    alert('환자 접수 취소 실패. 다시 시도하세요.');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('서버와의 통신 중 오류가 발생했습니다.');
            });

        cancelModal.hide();
    });

    // '아니요' 버튼 클릭 시 모달 닫기
    closeModalBtn.addEventListener("click", function () {
        cancelModal.hide();
    });
}




// 날짜를 선택할 때 환자 정보를 가져오는 이벤트 리스너
function callDate() {
    document.getElementById('currentDate').addEventListener('change', callPatientList);
}

// 환자 목록을 가져오는 함수
function callPatientList() {
    const selectedDate = this.value; // 선택한 날짜 가져오기
    console.log('선택한 날짜:', selectedDate); // 선택한 날짜 로그
    callPatientListRender(); // 날짜에 맞는 환자 목록 렌더링
}

function callPatientListRender() {
    // 각 상태별 환자 수 초기화
    let waitingCount = 0;
    let treatmentCount = 0;
    let completedCount = 0;

    // 첫 번째 행(헤더)을 제외한 모든 행 삭제
    const waitingPatientsBody = document.getElementById('waitingPatientsBody');
    const treatmentPatientsBody = document.getElementById('treatmentPatientsBody');
    const completedPatientsBody = document.getElementById('completedPatientsBody');

    // 테이블 내용 초기화
    waitingPatientsBody.innerHTML = '';
    treatmentPatientsBody.innerHTML = '';
    completedPatientsBody.innerHTML = '';

    const selectedDate = document.getElementById('currentDate').value;
    if (!selectedDate) {
        console.error('선택된 날짜가 없습니다.');
        return;
    }

    fetch(`/api/patient-admission/date/${selectedDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('네트워크 응답이 올바르지 않습니다.');
            }
            return response.json();
        })
        .then(data => {
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

                if (patient.treatStatus === '1') {
                    const doctorOptions = doctorNames.map(name => `<option value="${name}">${name}</option>`).join('');
                    const existingRows = waitingPatientsTable.getElementsByTagName('tr');
                    const isDuplicate = Array.from(existingRows).some(row => {
                        const pidCell = row.querySelector('td:nth-child(7)'); // 7번째 열 (pid 열)
                        return pidCell && pidCell.textContent === patient.pid.toString(); // pid 비교
                    });

                    if (isDuplicate) {
                        // console.log(`환자 ${patient.paName} (pid: ${patient.pid})는 이미 대기 목록에 존재합니다. 추가하지 않습니다.`);
                        return; // 중복된 환자는 추가하지 않음
                    }
                    waitingPatientsBody.innerHTML += `
                         <tr>
                            <td>${waitingPatientsBody.children.length + 1}</td>
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>
                                <select>
                                     ${doctorOptions}
                                </select>
                            </td>
                            <td>${formattedRvTime}</td>
                            <td>${formattedReceptionTime}</td>
                        </tr>
                    `;
                    waitingCount++;

                }

                if (patient.treatStatus === '2') {
                    treatmentPatientsBody.innerHTML += `
                        <tr>
                            <td>${treatmentPatientsBody.children.length + 1}</td>
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>${patient.mainDoc || 'N/A'}</td>
                            <td>${formattedReceptionTime}</td>
                            <td>${formattedViTime}</td>
                        </tr>
                    `;
                    treatmentCount++;
                }

                if (patient.treatStatus === '3') {
                    completedPatientsBody.innerHTML += `
                        <tr>
                            <td>${completedPatientsBody.children.length + 1}</td>
                            <td>${patient.chartNum || 'N/A'}</td>
                            <td>${patient.paName || 'N/A'}</td>
                            <td>${patient.mainDoc || 'N/A'}</td>
                            <td>${formattedReceptionTime}</td>
                            <td>${formattedCpTime}</td>
                        </tr>
                    `;
                    completedCount++;
                }
            });

            console.log(`대기 환자 수: ${waitingCount}`);
            console.log(`진료 중 환자 수: ${treatmentCount}`);
            console.log(`진료 중 환자 수:`, updateTreatmentCountLabel);
            console.log(`진료 완료 환자 수: ${completedCount}`);
            // 환자 수 업데이트
            updateWaitingCountLabel(waitingCount);
            updateTreatmentCountLabel(treatmentCount);
            updateCompleteCountLabel(completedCount);

        })
        .catch(error => {
            // console.error('문제가 발생했습니다:', error);
        });
}
// 카운트를 업데이트하는 함수들
function updateWaitingCountLabel(count) {
    const waitingHeader = document.querySelector("#waitingPatientsTable th[colspan='6']");
    if (waitingHeader) {
        waitingHeader.textContent = `진료 대기 환자: ${count}명`;
    }
}

function updateTreatmentCountLabel(count) {
    const treatmentHeader = document.querySelector("#treatmentPatientsTable th[colspan='6']");
    if (treatmentHeader) {
        treatmentHeader.textContent = `진료 중 환자: ${count}명`;
    }
}

function updateCompleteCountLabel(count) {
    const completedHeader = document.querySelector('#completedPatientsTable th[colspan="6"]');
    if (completedHeader) {
        completedHeader.textContent = `진료 완료 환자: ${count}명`;
    }
}




