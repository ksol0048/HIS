package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


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

    public ReservationDTO(Long seq, LocalDateTime reservationDate,
                          String department, boolean snsNotification, String chartNumber, String patientNote,
                          String reservationStatusCheck) {
        super();
        this.seq = seq;
        this.reservationDate = String.valueOf(reservationDate);
        this.department = department;
        this.snsNotification = snsNotification;
        this.chartNumber = chartNumber;
        this.patientNote = patientNote;
        this.reservationStatusCheck = reservationStatusCheck;
    }

}
