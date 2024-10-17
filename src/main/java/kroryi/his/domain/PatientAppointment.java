package kroryi.his.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "patient_appointments")
public class PatientAppointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chart_num", nullable = false)
    private Integer id;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "rv_type", length = 20)
    private String rvType;

    @Column(name = "pa_name", nullable = false, length = 20)
    private String paName;

    @Column(name = "main_doc", length = 20)
    private String mainDoc;

    @Column(name = "dental_hygienist", length = 20)
    private String dentalHygienist;

    @Column(name = "rv_time")
    private LocalDate rvTime;

    @Column(name = "md_time")
    private LocalDate mdTime;

    @Column(name = "medical_type", length = 100)
    private String medicalType;

    @Lob
    @Column(name = "pa_info")
    private String paInfo;

    @Lob
    @Column(name = "notice")
    private String notice;

    @Column(name = "rv_non", length = 20)
    private String rvNon;

    @Column(name = "rv_info", length = 100)
    private String rvInfo;

    @Column(name = "last_reserv")
    private LocalDate lastReserv;

    @Column(name = "first_visit")
    private LocalDate firstVisit;

    @Column(name = "last_visit")
    private LocalDate lastVisit;

}