package kroryi.his.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface PatientAdmissionService {
    PatientAdmission savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO) throws JsonProcessingException;

    List<PatientAdmission> getWaitingPatients();
    List<PatientAdmission> getTreatmentPatients();
    List<PatientAdmission> getCompletePatients();

    long getCompleteTreatmentCount(String count, LocalDate date);

    List<PatientAdmission> findByChartNumAndReceptionTime(Integer chartNum, LocalDateTime receptionTime);


    void updatePatientAdmission(PatientAdmission patientAdmission);

    List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate);

    void cancelAdmission(Integer pid);

    PatientAdmission getLatestCompletionTime(Integer chartNum);

    RedisTemplate<String, String> redisTemplate = null;
    void registerAdmission(PatientAdmission patientAdmission);

    void sendPatientCounts();

    long getPatientCountByDateAndStatus(String date, String status) throws ParseException;

    List<PatientAdmission> getWaitingPatientsForToday(LocalDate today);
    List<PatientAdmission> getTreatmentPatientsForToday(LocalDate today);
    List<PatientAdmission> getCompletePatientsForToday(LocalDate today);
}