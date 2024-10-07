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
    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate) {
        // 시작 시간과 종료 시간을 사용하여 데이터베이스에서 환자 접수 정보 조회
        List<PatientAdmission> admissions = patientAdmissionRepository.findByReceptionTimeBetween(startDate, endDate);

        // PatientAdmission 엔티티를 PatientAdmissionDTO로 변환
        return admissions.stream()
                .map(this::convertToDTO)
                .toList();
    }
    // PatientAdmission 엔티티를 PatientAdmissionDTO로 변환하는 메서드
    private PatientAdmissionDTO convertToDTO(PatientAdmission admission) {
        PatientAdmissionDTO dto = new PatientAdmissionDTO();
        dto.setChartNum(admission.getChartNum());
        dto.setPaName(admission.getPaName());
        dto.setMainDoc(admission.getMainDoc());
        dto.setReceptionTime(admission.getReceptionTime());
        dto.setRvTime(admission.getRvTime());
        dto.setTreatStatus(admission.getTreatStatus());
        dto.setViTime(admission.getViTime());
        dto.setCompletionTime(admission.getCompletionTime());
        return dto;
    }

}
