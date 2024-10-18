package kroryi.his.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "patient_admissions")
public class PatientAdmission {

    @Id
    @Column(name = "pid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pid;

    @Column(name = "chart_num", nullable = false)
    private Integer chartNum;

    @Column(name = "pa_name", nullable = false, length = 20)
    private String paName;

    @Column(name = "main_doc", length = 20)
    private String mainDoc;

    @Column(name = "rv_time")
    private LocalDateTime rvTime;

    @Column(name = "reception_time")
    private LocalDateTime receptionTime;

    @Column(name = "vi_time")
    private LocalDateTime viTime;

    @Column(name = "cp_time")
    private LocalDateTime completionTime;

    @Column(name = "treat_status", length = 100)
    private String treatStatus;

}