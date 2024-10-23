package kroryi.his.service.Impl;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.service.PatientAdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientAdmissionServiceImpl implements PatientAdmissionService {
    @Autowired
    private PatientAdmissionRepository patientAdmissionRepository;


    @Override
    public PatientAdmission savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO) {
            PatientAdmission patientAdmission = new PatientAdmission();
            patientAdmission.setChartNum(patientAdmissionDTO.getChartNum());
            patientAdmission.setPaName(patientAdmissionDTO.getPaName());
            patientAdmission.setMainDoc(patientAdmissionDTO.getMainDoc());
            patientAdmission.setReceptionTime(patientAdmissionDTO.getReceptionTime());
            patientAdmission.setRvTime(patientAdmissionDTO.getRvTime());
            patientAdmission.setViTime(patientAdmissionDTO.getViTime());


            if ("2".equals(patientAdmissionDTO.getTreatStatus())) {
                patientAdmission.setViTime(LocalDateTime.now());
            }
            patientAdmission.setCompletionTime(patientAdmissionDTO.getCompletionTime());
            patientAdmission.setTreatStatus(patientAdmissionDTO.getTreatStatus());

            return  patientAdmissionRepository.save(patientAdmission);
    }



    @Override
    public List<PatientAdmission> getWaitingPatients() {
        return patientAdmissionRepository.findByTreatStatus("1");
    }



    @Override
    public long getCompleteTreatmentCount(String count, LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay(); // 입력된 날짜의 00시 00분 00초
        LocalDateTime endDate = date.atTime(23, 59, 59);


        return patientAdmissionRepository.countByTreatStatusAndReceptionTimeBetween(count,startDate, endDate);
    }

    @Override
    public Optional<PatientAdmission> findByChartNumAndReceptionTime(Integer chartNum, LocalDateTime receptionTime) {
        // receptionTime에서 날짜만 추출하여 LocalDate로 변환
        LocalDate receptionDate = LocalDate.from(receptionTime);
        return patientAdmissionRepository.findByChartNumAndReceptionDate(chartNum, receptionDate);
    }




    @Override
    public void updatePatientAdmission(PatientAdmission patientAdmission) {
        patientAdmissionRepository.save(patientAdmission);
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
