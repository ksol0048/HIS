window.onload = function () {
    axios.get('/admin_management/') // 적절한 API 엔드포인트를 사용
        .then(response => {
            let members = response.data; // 서버에서 가져온 데이터
            // console.log(members)
            const tbody = document.querySelector('#membersTable tbody');
            // members가 배열이 아닐 경우 배열로 변환
            if (!Array.isArray(members)) {
                members = [members];
            }

            // 중첩된 데이터를 변환하는 로직
            const transformedMembers = members.map(member => {
                // // roles에서 중첩된 member 정보를 제거하고 새로운 객체 생성
                // console.log("1111" + member)
                // const transformedRoles = member.roles.map(roleSet => {
                //     console.log(roleSet)
                //     return {
                //         role: roleSet
                //     };
                // });
                //
                // console.log(transformedRoles)
                // // 중첩된 roles만 변환한 새로운 member 객체 반환
                // return {
                //     mid: member.mid,
                //     name: member.name,
                //     password: member.password,
                //     email: member.email,
                //     retirement: member.retirement,
                //     roles: transformedRoles
                // };
            });

            // 변환된 데이터 출력
            console.log(members);
            // 사용자 리스트를 반복하며 테이블에 추가
            members.forEach(user => {
                const row = document.createElement('tr');
                const roles = user.roles.map(role => role.roleSet).join(', ');

                row.innerHTML = `
                        <td>${user.mid}</td>
                        <td>${user.name}</td>
                        <td>${user.email}</td>
                        <td>${roles}</td> <!-- Set이나 배열을 문자열로 변환 -->
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
        name: document.getElementById('txtPopName').value,
        roles: [
            { roleSet: document.getElementById('cmbPopUserAuth').value }  // 단일 선택일 경우
        ],
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
document.getElementById("btnSearch").addEventListener("click", function () {
    // 입력된 검색 조건을 가져옴
    const userId = document.getElementById("txtId").value;
    const userName = document.getElementById("txtName").value;
    const userRole = document.getElementById("cmbAuth").value;
    const startDate = document.getElementById("transactionStartDate").value;

    // 검색 조건을 객체로 만들기
    const searchParams = {
        id: userId,
        name: userName,
        role: userRole,
        startDate: startDate
    };

    // AJAX 요청을 통해 서버로 검색 조건을 전송
    fetch("/admin_management/searchUsers", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(searchParams)
    })
        .then(response => response.json())
        .then(data => {
            // 서버로부터 받은 데이터를 이용해 사용자 리스트를 갱신
            updateUserTable(data);
        })
        .catch(error => console.error('Error:', error));
});

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

function searchUser() {
    const input = document.getElementById('search').value.toLowerCase();
    const table = document.getElementById('userTable');
    const rows = table.getElementsByTagName('tr');

    for (let i = 1; i < rows.length; i++) {
        const cells = rows[i].getElementsByTagName('td');
        let match = false;

        for (let j = 0; j < cells.length - 1; j++) {
            if (cells[j].textContent.toLowerCase().indexOf(input) > -1) {
                match = true;
                break;
            }
        }
        rows[i].style.display = match ? "" : "none";
    }
}
//
// document.getElementById("btnSearch").addEventListener("click", function () {
//     // 검색 조건을 가져옴
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
//             if (data.length > 0) {
//                 // 검색된 사용자가 있으면 첫 번째 사용자 정보를 수정 폼에 표시
//                 const user = data[0];  // 예를 들어 첫 번째 사용자 선택
//
//                 // 사용자 정보를 수정 창에 채우기
//                 document.getElementById("txtPopId").value = user.id;
//                 document.getElementById("txtPopPwd").value = "";  // 비밀번호는 보안상 빈칸으로 둠
//                 document.getElementById("cmbPopUserAuth").value = user.role;
//                 document.getElementById("txtPopName").value = user.username;
//                 document.getElementById("txtPopTel").value = user.phone || "";  // 전화번호
//                 document.getElementById("txtPopMail").value = user.email;
//                 document.getElementById("txtPop").value = user.address || "";  // 주소
//                 document.getElementById("note").value = user.note || "";  // 특이사항
//             } else {
//                 alert("검색된 사용자가 없습니다.");
//             }
//         })
//         .catch(error => console.error('Error:', error));
// });
