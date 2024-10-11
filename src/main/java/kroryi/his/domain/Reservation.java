package kroryi.his.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq")
    private long seq;

    @Column(name = "reservation_date")
    private String reservationDate;

    @Column(name = "reservation_status")
    private String reservationstatus;

    @Column(name = "patient_status")
    private String patientStatus;

    @Column(name = "department")
    private String department;

    @Column(name = "sms_notification")
    private boolean smsNotification;

    @Column(name = "chart_number")
    private String chartNumber;

    @Column(name = "patient_note")
    private String patientNote;

    @Column(name = "reservation_status_check")
    private String reservationStatusCheck;

    public Reservation() {
        super();

    }

    public Reservation(long seq, String reservationDate, String reservationstatus, String patientStatus,
                       String department, boolean smsNotification, String chartNumber, String patientNote,
                       String reservationStatusCheck) {
        super();
        this.seq = seq;
        this.reservationDate = reservationDate;
        this.reservationstatus = reservationstatus;
        this.patientStatus = patientStatus;
        this.department = department;
        this.smsNotification = smsNotification;
        this.chartNumber = chartNumber;
        this.patientNote = patientNote;
        this.reservationStatusCheck = reservationStatusCheck;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getReservationstatus() {
        return reservationstatus;
    }

    public void setReservationstatus(String reservationstatus) {
        this.reservationstatus = reservationstatus;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isSmsNotification() {
        return smsNotification;
    }

    public void setSmsNotification(boolean smsNotification) {
        this.smsNotification = smsNotification;
    }

    public String getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(String chartNumber) {
        this.chartNumber = chartNumber;
    }

    public String getPatientNote() {
        return patientNote;
    }

    public void setPatientNote(String patientNote) {
        this.patientNote = patientNote;
    }

    public String getReservationStatusCheck() {
        return reservationStatusCheck;
    }

    public void setReservationStatusCheck(String reservationStatusCheck) {
        this.reservationStatusCheck = reservationStatusCheck;
    }


}
