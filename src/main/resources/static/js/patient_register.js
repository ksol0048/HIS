const patient_register = document.getElementById("patient_reg")
const name = document.getElementById("name")
const firstPaResidentNum = document.getElementById("firstPaResidentNum")
const lastPaResidentNum = document.getElementById("lastPaResidentNum")
const birthDate = document.getElementById("birthDate")
const gender = document.getElementById("gender")
const defaultAddress = document.getElementById("defaultAddress")
const detailedAddress = document.getElementById("detailedAddress")
const mainDoc = document.getElementById("mainDoc")
const visitPath = document.getElementById("visitPath")
const category = document.getElementById("category")
const tendency = document.getElementById("tendency")
const firstVisit = document.getElementById("firstVisit")
const lastVisit = document.getElementById("lastVisit")
const chartNum = document.querySelector("#chartNum")
const homeNum1 = document.getElementById("homeNum");
const homeNum2 = document.getElementById("homeNum2");
const homeNum3 = document.getElementById("homeNum3");
const phoneNum1 = document.getElementById("phoneNum");
const phoneNum2 = document.getElementById("phoneNum2");
const phoneNum3 = document.getElementById("phoneNum3");
const emailLocal = document.getElementById("emailLocal");
const emailDomain = document.getElementById("emailDomain");

patient_register.addEventListener("click", (e) => {
    globalUserData.authorities.forEach(auth => {
        console.log(auth.authority);
    });

    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }


    // 환자 정보를 담은 객체
    const patientObj = {
        name: name.value,
        firstPaResidentNum: firstPaResidentNum.value,
        lastPaResidentNum: lastPaResidentNum.value,
        birthDate: birthDate.value,
        gender: gender.value,
        homeNum: `${homeNum1.value}-${homeNum2.value}-${homeNum3.value}`,
        phoneNum: `${phoneNum1.value}-${phoneNum2.value}-${phoneNum3.value}`,
        email: `${emailLocal.value}@${emailDomain.value}`,
        defaultAddress: defaultAddress.value,
        detailedAddress: detailedAddress.value,
        mainDoc: mainDoc.value,
        visitPath: visitPath.value,
        category: category.value,
        tendency: tendency.value,
        firstVisit: firstVisit.value,
        memos: [] // 메모 데이터를 담을 배열
    };

    function setPatientFields(result) {
        console.log("Received result:", result);

        // 단일 필드 값 설정
        name.value = result.name || '';
        chartNum.value = result.chartNum || '';
        firstPaResidentNum.value = result.firstPaResidentNum || '';
        lastPaResidentNum.value = result.lastPaResidentNum || '';
        birthDate.value = result.birthDate || '';
        gender.value = result.gender || '';
        defaultAddress.value = result.defaultAddress || '';
        mainDoc.value = result.mainDoc || '';
        visitPath.value = result.visitPath || '';
        category.value = result.category || '';
        tendency.value = result.tendency || '';
        firstVisit.value = result.firstVisit || '';


        lastVisit.value = result.lastVisit || '';

        // 자택전화 나누기
        const [homeNum1, homeNum2, homeNum3] = result.homeNum.split('-');
        document.getElementById("homeNum").value = homeNum1 || '';
        document.getElementById("homeNum2").value = homeNum2 || '';
        document.getElementById("homeNum3").value = homeNum3 || '';

        // 휴대전화 나누기
        const [phoneNum1, phoneNum2, phoneNum3] = result.phoneNum.split('-');
        document.getElementById("phoneNum").value = phoneNum1 || '';
        document.getElementById("phoneNum2").value = phoneNum2 || '';
        document.getElementById("phoneNum3").value = phoneNum3 || '';

        // 이메일 나누기
        const [emailLocalPart, emailDomainPart] = result.email.split('@');
        document.getElementById("emailLocal").value = emailLocalPart || '';
        document.getElementById("emailDomain").value = emailDomainPart || '';
    }


    if (chartNum.value) {
        modifyPatient(patientObj, chartNum.value).then(setPatientFields).catch(e => {
            alert("Exception....");
            console.log(e);
        });
    } else {
        // 메모 테이블에서 동적으로 추가된 모든 메모를 가져옴
        const table = document.getElementById('memoTable').getElementsByTagName('tbody')[0];
        const rows = Array.from(table.getElementsByTagName('tr')); // 모든 테이블 행 가져오기

        rows.forEach((row, index) => {
            // 각 행의 메모 날짜와 내용을 추출
            const mmo = row.querySelector(`#mmo_${index}`);
            const memoRegDate = row.querySelector(`#memoRegDate_${index}`);
            const memoContent = row.querySelector(`#memoContent_${index}`);

            // 각 메모를 배열에 추가
            if (memoRegDate && memoContent) {
                patientObj.memos.push({
                    mmo: mmo.value,
                    regDate: memoRegDate.value,
                    content: memoContent.value
                });
                console.log(mmo.value);
                console.log(memoContent.value);
            }
        });
        addPatient(patientObj).then(setPatientFields).catch(e => {
            alert("Exception....");
            console.log(e);
        });
    }

}, false)

// 주소등록
function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function (data) {
            // 팝업을 통한 검색 결과 항목 클릭 시 실행
            var addr = ''; // 주소_결과값이 없을 경우 공백
            var extraAddr = ''; // 참고항목

            //사용자가 선택한 주소 타입에 따라 해당 주소 값을 가져온다.
            if (data.userSelectedType === 'R') { // 도로명 주소를 선택
                addr = data.roadAddress;
            } else { // 지번 주소를 선택
                addr = data.jibunAddress;
            }

            if (data.userSelectedType === 'R') {
                if (data.bname !== '' && /[동|로|가]$/g.test(data.bname)) {
                    extraAddr += data.bname;
                }
                if (data.buildingName !== '' && data.apartment === 'Y') {
                    extraAddr += (extraAddr !== '' ? ', ' + data.buildingName : data.buildingName);
                }
                if (extraAddr !== '') {
                    extraAddr = ' (' + extraAddr + ')';
                }
            } else {
                document.getElementById("defaultAddress").value = '';
            }

            // 선택된 우편번호와 주소 정보를 input 박스에 넣는다.
            document.getElementById('zipp_code_id').value = data.zonecode;
            document.getElementById("defaultAddress").value = addr;
            document.getElementById("defaultAddress").value += extraAddr;
            document.getElementById("detailedAddress").focus(); // 우편번호 + 주소 입력이 완료되었음으로 상세주소로 포커스 이동
        }
    }).open();
}


// 테이블 기능-------------------------------------------------------------------------------

const table = document.getElementById('memoTable').getElementsByTagName('tbody')[0];

// 새 행 추가 함수
function addRow(mmo, date, content) {
    // Collect existing rows
    const rows = Array.from(table.rows);

    // Sort existing rows by date
    rows.sort((a, b) => {
        const dateA = new Date(a.cells[1].querySelector('input').value);
        const dateB = new Date(b.cells[1].querySelector('input').value);
        return dateA - dateB; // Ascending sort
    });

    // Clear the table
    while (table.rows.length > 0) {
        table.deleteRow(0);
    }

    // Re-assign IDs in descending order and add sorted rows back to the table
    const rowCount = rows.length;
    rows.forEach((row, index) => {
        const newIndex = rowCount - index; // Set new index in descending order
        row.cells[0].querySelector('input').id = 'mmo_' + newIndex;
        row.cells[1].querySelector('input').id = 'memoRegDate_' + newIndex;
        row.cells[2].querySelector('textarea').id = 'memoContent_' + newIndex;
        table.appendChild(row); // Append sorted row back to the table
    });

    // Create a new row with an ID of 0
    const newRowIndex = 0; // This will be the index for the new row
    const newRow = document.createElement('tr');
    newRow.classList.add('new-row');

    // Hidden input for mmo
    const hiddenCell = newRow.insertCell(0);
    const hiddenMmoInput = document.createElement('input');
    hiddenMmoInput.type = 'hidden';
    hiddenMmoInput.name = 'mmo';
    hiddenMmoInput.id = 'mmo_' + newRowIndex; // Use 0 for the new row
    hiddenMmoInput.value = mmo;
    hiddenCell.appendChild(hiddenMmoInput);
    hiddenCell.style.display = 'none';

    // Date input cell
    const dateCell = newRow.insertCell(1);
    const dateInput = document.createElement('input');
    dateInput.type = 'date';
    dateInput.className = 'form-control';
    dateInput.id = 'memoRegDate_' + newRowIndex; // Use 0 for the new row
    dateInput.name = 'memoRegDate';
    dateInput.value = date ? date : new Date().toISOString().split('T')[0];
    dateCell.appendChild(dateInput);

    // Memo input cell
    const memoCell = newRow.insertCell(2);
    const memoInput = document.createElement('textarea');
    memoInput.className = 'form-control';
    memoInput.id = 'memoContent_' + newRowIndex; // Use 0 for the new row
    memoInput.name = 'memoContent';
    memoInput.value = content || '';
    memoCell.appendChild(memoInput);

    // Delete and edit buttons cell
    const deleteCell = newRow.insertCell(3);
    // Edit button
    const editButton = document.createElement('button');
    editButton.className = 'btn btn-primary m-1';
    editButton.innerHTML = '<i class="bi bi-pencil"></i>';
    editButton.onclick = function () {
        editRow(this);
    };
    deleteCell.appendChild(editButton);

    // Delete button
    const deleteButton = document.createElement('button');
    deleteButton.className = 'btn btn-danger';
    deleteButton.innerHTML = '<i class="bi bi-trash"></i>';
    deleteButton.onclick = function () {
        deleteRow(this);
    };
    deleteCell.appendChild(deleteButton);

    // Append the new row to the table
    table.appendChild(newRow);
}


// 공통함수(등록/수정)
function collectMemoData() {
    const rowIndex = table.rows.length; // Current number of rows
    const memoList = []; // Array to hold memo information

    for (let i = 0; i < rowIndex; i++) {
        const memoRegDate = document.getElementById("memoRegDate_" + i);
        const memoContent = document.getElementById("memoContent_" + i);
        const mmo = document.getElementById("mmo_" + i);

        // Check if mmo is undefined
        if (mmo.value === "undefined") {
            console.log("메모 날짜 ->", memoRegDate.value);
            console.log("메모 내용 ->", memoContent.value);
            const memoObj = {
                memoCharNum: chartNum.value, // Chart number
                regDate: memoRegDate.value, // Registration date
                content: memoContent.value // Content
            };
            memoList.push(memoObj); // Add memo information to array
        }
    }

    console.log(memoList); // Log memo list
    return memoList; // Return the collected memo list
}

// 메모 저장 버튼 클릭 이벤트
document.getElementById('memo_reg').addEventListener('click', (e) => {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const memoList = collectMemoData(); // Collect memo data

    try {
        addMemo(memoList).then(result => {
            const rows = Array.from(table.getElementsByTagName('tr'));
            const totalRows = rows.length;
            rows.forEach((row, index) => {
                const reverseIndex = totalRows - 1 - index; // Calculate the reverse index
                const mmoCell = row.querySelector(`#mmo_${reverseIndex}`);

                console.log(row);
                console.log(reverseIndex); // Log the reverse index
                console.log(mmoCell);
                console.log(result[reverseIndex]);
                if (mmoCell.value === "undefined") {
                    console.log("!!!!!!!!!!!!")
                    mmoCell.value = result[reverseIndex]; // Assign value from result
                }
            });
        }); // Send all memos to the server
    } catch (e) {
        alert("Exception...");
        console.error(e);
    }
});


// 행 삭제 함수
function deleteRow(button) {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const row = button.parentNode.parentNode;
    const mmoCell = row.querySelector('input[name="mmo"]'); // Select the hidden input by name
    const mmoValue = mmoCell.value; // Get the value from the hidden input
    console.log(mmoValue);
    if (mmoValue !== "undefined") {
        removeMemo(mmoCell.value).then(result => {
            row.parentNode.removeChild(row);
            alert("삭제하였습니다.")
        }).catch(e => {
            alert("Exception....")
            console.log(e)
        })
    } else {
        row.parentNode.removeChild(row);

    }

}

// 행 수정 함수
function editRow(button) {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    const row = button.parentNode.parentNode;
    const mmoCell = row.querySelector('input[name="mmo"]');
    const memoRegDate = row.querySelector('input[name="memoRegDate"]');
    const memoContent = row.querySelector('textarea[name="memoContent"]');
    const mmoValue = mmoCell.value; // Get the value from the hidden input
    console.log(mmoValue);

    const memoObj = {
        memoCharNum: chartNum.value, // Chart number
        regDate: memoRegDate.value, // Registration date
        content: memoContent.value // Content
    };
    console.log(memoObj)

    modifyMemo(memoObj, mmoValue).then(result => {
        alert("수정하였습니다.")
    }).catch(e => {
        alert("Exception....")
        console.log(e)
    })
}

// 테이블 기능-----------------------------------------------------------------------------


// 생년월일 입력 필드와 나이 입력 필드 가져오기-----------------------
const birthDateInput = document.getElementById('birthDate');
const ageInput = document.getElementById('age');

function calculateAge(birthDate) {
    const today = new Date();
    const birthDateObj = new Date(birthDate);
    let age = today.getFullYear() - birthDateObj.getFullYear();
    const monthDifference = today.getMonth() - birthDateObj.getMonth();
    if (monthDifference < 0 || (monthDifference === 0 && today.getDate() < birthDateObj.getDate())) {
        age--;
    }
    return age;
}

birthDateInput.addEventListener('change', () => {
    const birthDate = birthDateInput.value;
    if (birthDate) {
        const age = calculateAge(birthDate);
        ageInput.value = age;
    } else {
        ageInput.value = '';
    }
});
// 생년월일 입력 필드와 나이 입력 필드 가져오기-----------------------

// 새로고침 버튼
document.querySelector("#refreshBtn").addEventListener("click", () => {
    name.value = '';
    chartNum.value = '';
    firstPaResidentNum.value = '';
    lastPaResidentNum.value = '';
    birthDate.value = '';
    gender.value = '';
    defaultAddress.value = '';
    mainDoc.value = '';
    visitPath.value = '';
    category.value = '';
    tendency.value = '';
    firstVisit.value = '';
    lastVisit.value = '';

    homeNum1.value = '';
    homeNum2.value = '';
    homeNum3.value = '';
    phoneNum1.value = '';
    phoneNum2.value = '';
    phoneNum3.value = '';
    emailLocal.value = '';
    emailDomain.value = '';

    const rows = table.getElementsByClassName('new-row');

    // Loop backwards to avoid index issues when removing
    while (rows.length > 0) {
        rows[0].parentNode.removeChild(rows[0]);
    }

});

// 환자 삭제
document.querySelector("#patient_del").addEventListener("click", () => {
    // 권한 체크를 직접 수행합니다.
    const hasPermission = globalUserData.authorities.some(auth =>
        auth.authority === 'ROLE_DOCTOR' || auth.authority === 'ROLE_NURSE'
    );

    // 권한이 없으면 경고 메시지를 표시하고 등록 과정을 중단합니다.
    if (!hasPermission) {
        alert("권한이 없습니다. 의사 또는 간호사만 환자를 등록할 수 있습니다.");
        return; // 등록 과정 중단
    }

    removePatient(chartNum.value).then(result => {
        name.value = '';
        chartNum.value = '';
        firstPaResidentNum.value = '';
        lastPaResidentNum.value = '';
        birthDate.value = '';
        gender.value = '';
        defaultAddress.value = '';
        mainDoc.value = '';
        visitPath.value = '';
        category.value = '';
        tendency.value = '';
        firstVisit.value = '';

        homeNum1.value = '';
        homeNum2.value = '';
        homeNum3.value = '';
        phoneNum1.value = '';
        phoneNum2.value = '';
        phoneNum3.value = '';
        emailLocal.value = '';
        emailDomain.value = '';

        const rows = table.getElementsByClassName('new-row');

        // Loop backwards to avoid index issues when removing
        while (rows.length > 0) {
            rows[0].parentNode.removeChild(rows[0]);
        }
    }).catch(e => {
        alert("Exception....")
        console.log(e)
    })

}, false);
