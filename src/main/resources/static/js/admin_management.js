window.onload = function () {
    axios.get('/admin_management/') // 적절한 API 엔드포인트를 사용
        .then(response => {
            const members = response.data; // 서버에서 가져온 데이터
            const tbody = document.querySelector('#membersTable tbody');

            // 사용자 리스트를 반복하며 테이블에 추가
            members.forEach(user => {
                const row = document.createElement('tr');
                row.innerHTML = `
                        <td>${user.mid}</td>
                        <td>${user.name}</td>
                        <td>${user.email}</td>
                        <td>${user.roles.join(', ')}</td> <!-- Set이나 배열을 문자열로 변환 -->
                    `;
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching members:', error);
        });
};

// 저장 버튼 클릭 이벤트
document.getElementById('userTable').addEventListener('click', function (event) {
    event.preventDefault(); // 기본 동작 방지


    // 저장할 사용자 데이터 객체 생성
    const userData = {
        // 사용자 입력값 가져오기
        mid: document.getElementById('txtPopId').value,
        password: document.getElementById('txtPopPwd').value,
        roles: Array.from(document.getElementById('cmbPopUserAuth').value),
        // roles: document.getElementById('cmbPopUserAuth').value,
        name: document.getElementById('txtPopName').value,
        // phone : document.getElementById('txtPopTel').value,
        email: document.getElementById('txtPopMail').value,
        // address: document.getElementById('txtPop').value,
        // note : document.getElementById('note').value
    };
    console.log(userData)
    // 필수 입력값 체크
    if (!userData.mid || !userData.password || !userData.name || !userData.email) {
        alert('필수 입력값을 확인하세요.');
        return;
    }
    // 서버에 사용자 데이터 전송
    fetch('/admin_management/save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData),
    })
        .then(response => response.json())
        .then(data => {
            console.log(data)
            if (data.success) {
                alert('사용자가 성공적으로 저장되었습니다.');
                window.location.reload();
            } else {
                alert('저장에 실패했습니다. 다시 시도해주세요.');
                console.error('Error:', data.error);
            }
        })
        .catch((error) => {
            console.error('Error:', error);
            alert('저장 중 오류가 발생했습니다.');
        });
});

document.getElementById("btnDelete").addEventListener("click", function () {
    const userId = this.getAttribute("data-id");

    if (confirm("정말 삭제하시겠습니까?")) {
        fetch(`/delete/${userId}`, {
            method: 'POST', // DELETE로 하려면 method: 'DELETE' 사용
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value // CSRF 보호
            }
        })
            .then(response => {
                if (response.ok) {
                    alert("삭제되었습니다.");
                    window.location.href = "/users"; // 삭제 후 리다이렉트
                } else {
                    alert("삭제에 실패했습니다.");
                }
            })
            .catch(error => console.error('Error:', error));
    }
});
// document.getElementById("btnSearch").addEventListener("click", function () {
//     // 입력된 검색 조건을 가져옴
//     const userId = document.getElementById("txtId").value;
//     const userName = document.getElementById("txtName").value;
//     const userRole = document.getElementById("cmbAuth").value;
//     const startDate = document.getElementById("transactionStartDate").value;
//
//     // 검색 조건을 객체로 만들기
//     const searchParams = {
//         id: userId,
//         name: userName,
//         role: userRole,
//         startDate: startDate
//     };
//
//     // AJAX 요청을 통해 서버로 검색 조건을 전송
//     fetch("/searchUsers", {
//         method: "POST",
//         headers: {
//             "Content-Type": "application/json"
//         },
//         body: JSON.stringify(searchParams)
//     })
//         .then(response => response.json())
//         .then(data => {
//             // 서버로부터 받은 데이터를 이용해 사용자 리스트를 갱신
//             updateUserTable(data);
//         })
//         .catch(error => console.error('Error:', error));
// });

function updateUserTable(users) {
    const tbody = document.querySelector("table tbody");
    tbody.innerHTML = ""; // 기존 데이터 삭제

    // 새로운 사용자 리스트 추가
    users.forEach(user => {
        const row = document.createElement("tr");

        row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.username}</td>
                <td>${user.email}</td>
                <td>${user.role}</td>
                <td>${user.password}</td>
            `;
        tbody.appendChild(row);
    });
}

function saveNote() {
    const note = document.getElementById('note').value;
    if (note) {
        const savedNotesDiv = document.getElementById('savedNotes');
        const noteElement = document.createElement('div');
        noteElement.textContent = note;
        savedNotesDiv.appendChild(noteElement);
        document.getElementById('note').value = ''; // 입력창 비우기
    } else {
        alert('메모를 입력해주세요.');
    }
}

function addUser() {
    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const role = document.getElementById('role').value;

    const userTable = document.getElementById('userTable').getElementsByTagName('tbody')[0];
    const newRow = userTable.insertRow();
    newRow.innerHTML = `<td>${userTable.rows.length}</td><td>${name}</td><td>${email}</td><td>${role}</td><td><button onclick="editUser(${userTable.rows.length - 1})">수정</button> <button onclick="deleteUser(this)">삭제</button></td>`;

    document.getElementById('addUserForm').reset(); // 폼 초기화
}

function editUser(rowIndex) {
    // 수정 기능을 구현하는 코드
}

function deleteUser(button) {
    const row = button.parentNode.parentNode;
    row.parentNode.removeChild(row);
}

document.addEventListener("DOMContentLoaded", function () {
    const searchButton = document.getElementById("btnSearch");
    // const searchFieldTxtId = document.getElementById("txtId");
    // const searchFieldTxtName = document.getElementById("txtName");
    const patientTableBody = document.querySelector("#patientTableBody");
    const patientSearchKeyword = document.querySelector("#patient_name_keyword");
    let selectedRow = null;
    let patientData = [];


    // 클릭 이벤트 바인딩
    // searchButton.addEventListener("click", searchUser);

    // 입력 시 자동 검색 이벤트 바인딩
    // 입력 시 자동 검색 이벤트 바인딩
    // searchFieldTxtId.addEventListener("input", searchUser);
    // searchFieldTxtName.addEventListener("input", searchUser);

    // Patient search based on keyword
    document.querySelector("#addReplyBtn").addEventListener("click", () => {
        const keyword = {"keyword": patientSearchKeyword.value};

        patientSearch(keyword)
            .then(result => {
                let found = false;
                patientTableBody.innerHTML = "";  // Clear previous rows
                patientData = result;  // Store search results

                result.forEach((patient, index) => {
                    if (patient.name && patient.name.includes(keyword.keyword)) {
                        found = true;

                        const row = document.createElement("tr");
                        const birthDate = patient.birthDate ? patient.birthDate : '-';

                        row.innerHTML = `
                            <th scope="row">${index + 1}</th>
                            <td>${patient.id}</td>
                            <td>${calculateAge(birthDate)}</td>
                            <td>${patient.name}</td>
                            <td>${patient.chartNum}</td>
                            <td>${birthDate}</td>
                            <td>${patient.role}</td>
                        `;

                        row.addEventListener("click", () => {
                            if (selectedRow) {
                                selectedRow.classList.remove('selected');
                            }
                            selectedRow = row;
                            selectedRow.classList.add('selected');
                        });

                        patientTableBody.appendChild(row);
                        searchModal.show();
                    }
                });

                if (!found) {
                    alert("사용자가 존재하지 않습니다.");
                }
            })
            .catch(error => {
                console.error("사용자 데이터 가져오기 오류:", error);
                alert("사용자 검색 중 오류가 발생했습니다.");
            });
    });
    //
    // // Function to search users in the table
    // function searchUser() {
    //     const input = searchFieldTxtId.value.toLowerCase();
    //     const table = document.getElementById('userTable');
    //     const rows = table.getElementsByTagName('tr');
    //
    //
    //
    //     // Iterate over the rows (skip the first row for headers)
    //     for (let i = 1; i < rows.length; i++) {
    //         const cells = rows[i].getElementsByTagName('td');
    //         let match = false;
    //
    //         // Check if any cell matches the input
    //         for (let j = 0; j < cells.length; j++) {
    //             if (cells[j].textContent.toLowerCase().includes(input)) {
    //                 match = true;
    //                 break;
    //             }
    //         }
    //
    //         // Show or hide rows based on match
    //         rows[i].style.display = match ? "" : "none";
    //     }
    // }

    // Handle form search and AJAX request
    searchButton.addEventListener("click", function () {
        const searchParams = {
            id: document.getElementById("txtPopId").value,
            name: document.getElementById("txtPopName").value,
            role: document.getElementById("cmbPopUserAuth").value,
            startDate: document.getElementById("transactionStartDate").value
        };

        fetch("/admin_management/searchUsers", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(searchParams)
        })
            .then(response => response.json())
            .then(data => {
                console.log(data); // 응답 데이터 확인
                if (data.length > 0) {
                    const user = data[0];

                    // Fill the form with user details
                    document.getElementById("txtPopId").value = user.id;
                    document.getElementById("txtPopPwd").value = "";  // Leave password blank for security
                    document.getElementById("cmbPopUserAuth").value = user.role;
                    document.getElementById("txtPopName").value = user.username;
                    document.getElementById("txtPopTel").value = user.phone || "";
                    document.getElementById("txtPopMail").value = user.email;
                    document.getElementById("txtPop").value = user.address || "";
                    document.getElementById("note").value = user.note || "";
                } else {
                    alert("검색된 사용자가 없습니다.");
                }
            })
            .catch(error => {
                console.error('사용자 검색 중 오류 발생:', error);
                alert('네트워크 상태를 확인해 주세요.');
            });
    });
});

