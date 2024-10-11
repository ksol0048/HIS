package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ReservationDTO {
    private Long seq;
    private String reservationDate;
    private String reservationstatus;
    private String patientStatus;
    private String department;
    private boolean smsNotification;
    private String chartNumber;
    private String patientNote;
    private String reservationStatusCheck;

    public ReservationDTO() {
        super();
    }

    public ReservationDTO(Long seq, String reservationDate, String reservationstatus, String patientStatus,
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

}
