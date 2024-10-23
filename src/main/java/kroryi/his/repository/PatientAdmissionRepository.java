package kroryi.his.repository;

import com.querydsl.core.group.GroupBy;
import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.dto.ReservationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientAdmissionRepository extends JpaRepository<PatientAdmission, Integer> {
    List<PatientAdmission> findByTreatStatus(String treatStatus);

    List<PatientAdmission> findByReceptionTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByTreatStatusAndReceptionTimeBetween(String count, LocalDateTime startDate, LocalDateTime endDate);


    @Query("SELECT p FROM PatientAdmission p WHERE p.chartNum = :chartNum AND DATE(p.receptionTime) = :receptionDate")
    Optional<PatientAdmission> findByChartNumAndReceptionDate(@Param("chartNum") Integer chartNum, @Param("receptionDate") LocalDate receptionDate);

    @Query("SELECT pa FROM PatientAdmission pa WHERE pa.chartNum = :chartNum ORDER BY pa.completionTime DESC LIMIT 1")
    PatientAdmission findLatestByChartNum(@Param("chartNum") Integer chartNum);

}
