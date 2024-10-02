package kroryi.his.service;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import java.util.List;


public interface PatientAdmissionService {
    void savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);
    void completePatientAdmission(PatientAdmissionDTO patientAdmissionDTO);
    List<PatientAdmission> getWaitingPatients();


}
