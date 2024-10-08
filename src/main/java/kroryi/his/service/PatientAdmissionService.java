package kroryi.his.service;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;

import java.time.LocalDateTime;
import java.util.List;


public interface PatientAdmissionService {
    void savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);

    List<PatientAdmission> getWaitingPatients();

    List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate);
}
