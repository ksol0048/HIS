package kroryi.his.service;

import kroryi.his.domain.PatientRegister;
import kroryi.his.domain.PatientRegisterMemo;
import kroryi.his.dto.PatientDTO;
import kroryi.his.dto.PatientMemoDTO;

import java.util.List;

public interface PatientRegisterService {
    String generateChartNum();

//    PatientRegister registerPatient();

    PatientRegister register(PatientDTO patientDTO);

//    Long register(PatientDTO patientDTO);
//    List<PatientRegister> searchName(String keyword);

    List<PatientRegister> searchNameByKeyword(String keyword);

    void remove(String chartNum);

    PatientRegister modify(PatientDTO patientDTO);

    PatientRegister getPatient(String chartNum);

    // DOCTOR 역할을 가진 회원의 이름을 가져오는 메소드
    public List<String> getDoctorNames() ;

}