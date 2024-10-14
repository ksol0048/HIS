package kroryi.his.repository;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PatientAdmissionRepository extends JpaRepository<PatientAdmission, Integer> {
    List<PatientAdmission> findByTreatStatus(String treatStatus);

    List<PatientAdmission> findByReceptionTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

//    boolean existsByChartNum(Integer chartNum); // 차트 번호로 존재 여부 체크
//
//    PatientAdmission findByChartNum(Integer chartNum); // 차트 번호로 환자 정보 조회
}
