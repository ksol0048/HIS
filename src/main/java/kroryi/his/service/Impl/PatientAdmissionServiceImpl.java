package kroryi.his.service.Impl;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.service.PatientAdmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PatientAdmissionServiceImpl implements PatientAdmissionService {
    private static final Logger log = LoggerFactory.getLogger(PatientAdmissionServiceImpl.class);
    @Autowired
    private PatientAdmissionRepository patientAdmissionRepository;


    @Transactional
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

        if (patientAdmission.getPid() != null ) {
            throw new IllegalArgumentException("ID는 null이 아니어야 합니다."); // 예외 처리
        }

            return  patientAdmissionRepository.save(patientAdmission);
    }



    @Override
    public List<PatientAdmission> getWaitingPatients() {
        return patientAdmissionRepository.findByTreatStatus("1");
    }



    @Transactional
    @Override
    public long getCompleteTreatmentCount(String count, LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay(); // 입력된 날짜의 00시 00분 00초
        LocalDateTime endDate = date.atTime(23, 59, 59);
        try {
            // 환자 정보 처리
            return patientAdmissionRepository.countByTreatStatusAndReceptionTimeBetween(count,startDate, endDate);
        } catch (Exception e) {
            // 예외 처리 시 세션 플러시 방지
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("처리 중 오류 발생", e);
        }
    }

    @Override
    public List<PatientAdmission> findByChartNumAndReceptionTime(Integer chartNum, LocalDateTime receptionTime) {
        // receptionTime에서 날짜만 추출하여 LocalDate로 변환
        LocalDate receptionDate = LocalDate.from(receptionTime);

        // 차트 번호와 날짜를 기준으로 환자 리스트 조회
        List<PatientAdmission> patientAdmissions = patientAdmissionRepository.findByChartNumAndReceptionDate(chartNum, receptionDate);

        // 중복된 환자 정보를 처리하여 필요한 경우에 맞게 리턴
        return patientAdmissions; // 여기에 추가적인 로직을 넣어도 됩니다. (예: 가장 최근 환자만 선택)
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

    @Override
    @Transactional
    public void cancelAdmission(Integer pid) {
        // 환자 삭제 로직
        if (!patientAdmissionRepository.existsById(Math.toIntExact(pid))) {
            throw new RuntimeException("환자를 찾을 수 없습니다."); // 예외 처리
        }
        patientAdmissionRepository.deleteById(Math.toIntExact(pid));
    }

    @Override
    public PatientAdmission getLatestCompletionTime(Integer chartNum) {
        PatientAdmission admissions = patientAdmissionRepository.findLatestByChartNum(chartNum);
        log.info(admissions != null ? admissions.toString() : "chartNum {}에 대한 환자 데이터가 없습니다.", chartNum);
        return admissions; // 데이터가 없으면 null을 반환
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
