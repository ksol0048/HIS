package kroryi.his.service;

import kroryi.his.domain.PatientRegister;
import kroryi.his.dto.PatientDTO;

public interface PatientRegisterService {
    String generateChartNum();

    PatientRegister registerPatient();

    PatientRegister register(PatientDTO patientDTO);

//    Long register(PatientDTO patientDTO);
}