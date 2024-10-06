package kroryi.his.dto;

import kroryi.his.domain.PatientAdmission;
import lombok.*;

import java.io.Serializable;

import java.time.LocalDateTime;

/**
 * DTO for {@link kroryi.his.domain.PatientAdmission}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientAdmissionDTO implements Serializable {
    private Integer chartNum;
    private String paName;
    private String mainDoc;
    private LocalDateTime rvTime;  //예약시간
    private LocalDateTime receptionTime;  //접수시간
    private LocalDateTime viTime;  // 진료 시작 시간
    private LocalDateTime completionTime;  //진료 완료 시간
    private String treatStatus;

    // PatientAdmission 객체를 받아들이는 생성자 추가
    public PatientAdmissionDTO(PatientAdmission patient) {
        this.chartNum = patient.getChartNum();
        this.paName = patient.getPaName();
        this.mainDoc = patient.getMainDoc();
        this.rvTime = patient.getRvTime();
        this.receptionTime = patient.getReceptionTime();
        this.viTime = patient.getViTime();
        this.completionTime = patient.getCompletionTime(); // cp_time
        this.treatStatus = patient.getTreatStatus();
    }
}