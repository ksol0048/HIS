package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ReservationDTO {
    private Long seq;
    private String reservationDate;
    private String department;
    private boolean snsNotification;
    private String chartNumber;
    private String patientNote;
    private String reservationStatusCheck;

    public ReservationDTO() {
        super();
    }

    public ReservationDTO(Long seq, String reservationDate,
                          String department, boolean snsNotification, String chartNumber, String patientNote,
                          String reservationStatusCheck) {
        super();
        this.seq = seq;
        this.reservationDate = reservationDate;
        this.department = department;
        this.snsNotification = snsNotification;
        this.chartNumber = chartNumber;
        this.patientNote = patientNote;
        this.reservationStatusCheck = reservationStatusCheck;
    }

}
