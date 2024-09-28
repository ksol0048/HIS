const searchModal = new bootstrap.Modal(document.querySelector(".SearchModal"))
const closeBtn = document.querySelector(".closeBtn")
const patient_name_keyword = document.querySelector("#patient_name_keyword")


let selectedRow = null; // 클릭된 행을 저장할 변수

let patientData = null; // 전역 변수 선언

let selectedMemos = null;

document.querySelector("#addReplyBtn").addEventListener("click", (e) => {
    const keyword = {
        "keyword": patient_name_keyword.value
    };

    patientSearch(keyword).then(result => {
        let found = false;
        const tableBody = document.querySelector("#patientTableBody");
        tableBody.innerHTML = "";  // 기존 행을 지웁니다
        patientData = result; // 검색된 환자 정보를 전역 변수에 저장

        result.forEach((patient, index) => {
            if (patient.name.includes(keyword.keyword)) {
                found = true;

                // 새 행 생성
                const row = document.createElement("tr");
                const birthDate = patient.birthDate ? patient.birthDate : '-';

                row.innerHTML = `
                    <th scope="row">${index + 1}</th>
                    <td>${patient.name}</td>
                    <td>${calculateAge(birthDate)}</td>
                    <td>${patient.gender === 'm' ? '남성' : '여성'}</td>
                    <td>${patient.chartNum}</td>
                    <td>${birthDate}</td>
                    <td>${patient.phoneNum}</td>
                `;

                row.addEventListener("click", () => {
                    if (selectedRow) {
                        selectedRow.classList.remove('selected');
                    }
                    selectedRow = row;
                    selectedRow.classList.add('selected');
                });

                tableBody.appendChild(row);
                searchModal.show();
            }
        });
        if (!found) {
            alert("환자가 존재하지 않습니다.");
        }
    }).catch(error => {
        console.error("환자 데이터 가져오기 오류:", error);
    });
}, false);

document.querySelector(".SearchBtn").addEventListener("click", () => {
    if (selectedRow) {
        // 선택된 행의 데이터 가져오기
        const menu_name = selectedRow.querySelector("td:nth-child(2)").textContent;
        const menu_age = calculateAge(selectedRow.querySelector("td:nth-child(6)").textContent);
        const menu_gender = selectedRow.querySelector("td:nth-child(4)").textContent;
        const menu_chartNum = selectedRow.querySelector("td:nth-child(5)").textContent;
        const menu_birthDate = selectedRow.querySelector("td:nth-child(6)").textContent;

        // HTML 요소에 데이터 삽입
        document.querySelector("#patientInfo").innerHTML = `
            <div class="text-center row">
                <label>이름: ${menu_name} (${menu_age}, ${menu_gender})</label>
                <label>차트번호: ${menu_chartNum}</label>
                <label>생일: ${menu_birthDate || '-'}</label>
            </div>
        `;


        const ageInput = document.getElementById('age');
        if (window.location.href.includes("/patient_register")) {
            patientData.forEach((patient, index) => {
                if (menu_chartNum === patient.chartNum) {
                    selectedMemos = patient.memos;
                    test(selectedMemos);
                    name.value = patient.name || '';
                    firstPaResidentNum.value = patient.firstPaResidentNum || '';
                    lastPaResidentNum.value = patient.lastPaResidentNum || '';
                    birthDate.value = patient.birthDate || '';
                    gender.value = patient.gender || '';
                    defaultAddress.value = patient.defaultAddress || '';
                    detailedAddress.value = patient.detailedAddress || '';
                    mainDoc.value = patient.mainDoc || '';
                    visitPath.value = patient.visitPath || '';
                    category.value = patient.category || '';
                    tendency.value = patient.tendency || '';
                    firstVisit.value = patient.firstVisit || '';
                    lastVisit.value = patient.lastVisit || '';
                    chartNum.value = patient.chartNum;
                    ageInput.value = menu_age;

                    // 자택전화 나누기
                    const [home_Num1, home_Num2, home_Num3] = patient.homeNum.split('-');
                    homeNum1.value = home_Num1 || '';
                    homeNum2.value = home_Num2 || '';
                    homeNum3.value = home_Num3 || '';

                    // 휴대전화 나누기
                    const [phone_Num1, phone_Num2, phone_Num3] = patient.phoneNum.split('-');
                    phoneNum1.value = phone_Num1 || '';
                    phoneNum2.value = phone_Num2 || '';
                    phoneNum3.value = phone_Num3 || '';

                    // 이메일 나누기
                    const [emailLocalPart, emailDomainPart] = patient.email.split('@');
                    emailLocal.value = emailLocalPart || '';
                    emailDomain.value = emailDomainPart || '';
                }
            })
        }


        // 세션 저장
        sessionStorage.setItem('selectedPatient', JSON.stringify({
            name: menu_name,
            age: menu_age,
            gender: menu_gender,
            chartNum: menu_chartNum,
            birthDate: menu_birthDate
        }));


        // 모달 창 닫기
        searchModal.hide();
    } else {
        console.log("선택된 행이 없습니다.");
    }
}, false);

// 생일로부터 나이 계산 함수
function calculateAge(birthDate) {
    if (!birthDate || birthDate === '-') return '-'; // 생일이 없거나 '-'인 경우
    const birthYear = new Date(birthDate).getFullYear();
    const currentYear = new Date().getFullYear();
    return currentYear - birthYear;
}


closeBtn.addEventListener("click", (e) => {
    searchModal.hide()
}, false)

function test() {
    if (selectedMemos.length > 0) {
        selectedMemos.forEach((memo) => {
            console.log(memo.content);
            // 마지막 메모로 입력란을 채우고 싶다면
            memo_date.value = memo.regDate;
            memo_textarea.value = memo.content;
            addRow(memo.regDate, memo.content);
        });
    }
}

