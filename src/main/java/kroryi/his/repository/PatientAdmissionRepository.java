package kroryi.his.repository;

import kroryi.his.domain.PatientAdmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientAdmissionRepository extends JpaRepository<PatientAdmission, Integer> {
    List<PatientAdmission> findByTreatStatus(String treatStatus);

    List<PatientAdmission> findByTreatStatusIs(String treatStatus);

    List<PatientAdmission> findByTreatStatusIsNot(String treatStatus);

    List<PatientAdmission> findByCompletionTime(LocalDateTime completionTime);

    List<PatientAdmission> findByCompletionTimeBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);
}
