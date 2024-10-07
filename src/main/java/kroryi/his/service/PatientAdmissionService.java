package kroryi.his.service;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;

import java.time.LocalDateTime;
import java.util.List;


public interface PatientAdmissionService {
    void savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);
    void completePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);
    List<PatientAdmission> getWaitingPatients();


    // 진료 중 환자 목록을 반환
    List<PatientAdmission> getWaitingPatientsForTreatment();

    // 진료 완료 환자 목록을 반환
    List<PatientAdmission> getCompletedPatients();

    List<PatientAdmission> getPatientsByStatus(String status);



    List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate);
}
