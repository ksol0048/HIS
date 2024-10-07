package kroryi.his.service.Impl;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.service.PatientAdmissionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientAdmissionServiceImpl implements PatientAdmissionService {
    @Autowired
    private PatientAdmissionRepository patientAdmissionRepository;

    private ModelMapper modelMapper;

    @Override
    public void savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO) {
        try {
            PatientAdmission patientAdmission = new PatientAdmission();
            patientAdmission.setChartNum(patientAdmissionDTO.getChartNum());
            patientAdmission.setPaName(patientAdmissionDTO.getPaName());
            patientAdmission.setMainDoc(patientAdmissionDTO.getMainDoc());
            patientAdmission.setReceptionTime(patientAdmissionDTO.getReceptionTime());
            patientAdmission.setRvTime(patientAdmissionDTO.getRvTime());
            patientAdmission.setViTime(patientAdmissionDTO.getViTime());

            // viTime을 현재 시간으로 설정
            if ("2".equals(patientAdmissionDTO.getTreatStatus())) { // 진료 중 상태일 때
                patientAdmission.setViTime(LocalDateTime.now());
            }
            patientAdmission.setCompletionTime(patientAdmissionDTO.getCompletionTime());
            patientAdmission.setTreatStatus(patientAdmissionDTO.getTreatStatus());

            patientAdmissionRepository.save(patientAdmission);
        } catch (Exception e) {
            System.err.println("저장 중 오류 발생: " + e.getMessage());
            throw e; // 또는 사용자 정의 예외를 발생시킬 수 있습니다.
        }
    }

    @Override
    public void completePatientAdmission(PatientAdmissionDTO patientAdmissionDTO) {
        List<PatientAdmission> admissions = patientAdmissionRepository.findByTreatStatus("2");

        for(PatientAdmission admission : admissions) {
            admission.setCompletionTime(patientAdmissionDTO.getCompletionTime());
            admission.setTreatStatus("3");
            patientAdmissionRepository.save(admission);
        }

    }

    @Override
    public List<PatientAdmission> getWaitingPatients() {
        return patientAdmissionRepository.findByTreatStatus("1");
    }

    // 진료 중 환자 목록을 반환
    @Override
    public List<PatientAdmission> getWaitingPatientsForTreatment() {
        return patientAdmissionRepository.findByTreatStatus("2"); // "2"는 진료 중 상태
    }

    // 진료 완료 환자 목록을 반환
    @Override
    public List<PatientAdmission> getCompletedPatients() {
        return patientAdmissionRepository.findByTreatStatus("3"); // "3"은 진료 완료 상태
    }



    @Override
    public List<PatientAdmission> getPatientsByStatus(String status) {
        return patientAdmissionRepository.findByTreatStatus(status);
    }

    @Override
    public List<PatientAdmissionDTO> getCompletedPatientsByDate(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        List<PatientAdmission> patients = patientAdmissionRepository.findByCompletionTimeBetween(startOfDay, endOfDay);
        return patients.stream()
                .map(PatientAdmissionDTO::new) // DTO 변환
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate) {
        // 접수 시간 범위에 맞는 환자 접수 정보 가져오기
        List<PatientAdmission> admissions = patientAdmissionRepository.findByReceptionTimeBetween(startDate, endDate);

        // 엔티티 리스트를 DTO 리스트로 변환
        return admissions.stream()
                .map(admission -> modelMapper.map(admission, PatientAdmissionDTO.class))
                .collect(Collectors.toList());
    }


}
