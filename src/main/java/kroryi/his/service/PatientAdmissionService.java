package kroryi.his.service;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface PatientAdmissionService {
    void savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);

    List<PatientAdmission> getWaitingPatients();

    List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByChartNum(Integer chartNum); // 차트 번호로 존재 여부 체크

    PatientAdmission findByChartNum(Integer chartNum); // 차트 번호로 환자 정보 조회
}
