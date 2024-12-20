package kroryi.his.domain;

import jakarta.persistence.*;
import kroryi.his.websocket.PatientAdmissionListener;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@EntityListeners(PatientAdmissionListener.class)
@Table(name = "patient_admissions")
public class PatientAdmission {

    @Id
    @Column(name = "pid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pid;

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

    public void setMessage(String s) {

    }
}