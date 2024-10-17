async function addPatient(patientObj) {
    const response = await axios.post(`/patient_register/`, patientObj)
    console.log("44444440->" + response)
    return response.data.patientRegister;
}

async function patientSearch(keyword) {
    const response = await axios.post(`/patient_search/`, keyword)
    console.log(response)
    return response.data.result;
}

async function removePatient(chartNum) {
    const response = await axios.delete(`/patient_register/remove/${chartNum}`)
    console.log(response)
    return response.data.result;
}

async function modifyPatient(patientObj,chartnum) {
    const response = await axios.put(`/patient_register/modify/${chartnum}`, patientObj)
    console.log(response)
    return response.data.patientRegister;
}

async function addMemo(memoList) {
    const response = await axios.post(`/patient_register/memo/`, memoList); // memos 배열로 감싸서 전송
    console.log(response);
    return response.data; // 서버의 응답에서 결과를 반환
}

async function removeMemo(mmo) {
    const response = await axios.delete(`/patient_register/memo/remove/${mmo}`)
    console.log(response)
    return response.data;
}

async function modifyMemo(memoObj,mmo) {
    const response = await axios.put(`/patient_register/memo/modify/${mmo}`, memoObj)
    console.log(response)
    return response.data;
}

async function patientMaintenance(chartNum) {
    try {
        const response = await axios.post(`/patient_search/${chartNum}`, {}, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        console.log(response);
        return response.data.result;
    } catch (error) {
        console.error("Error fetching patient:", error);
        throw error;
    }
}


