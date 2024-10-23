package kroryi.his.service;

import kroryi.his.dto.ReservationDTO;

import java.util.List;

public interface ReservationService {
    List<ReservationDTO> selectedDatePatientList(ReservationDTO dto);
    List<ReservationDTO> selectedByReservation(ReservationDTO dto);
    List<ReservationDTO> insertReservationInformation(ReservationDTO dto);
    void updateReservationInformation(ReservationDTO dto);
    void deleteReservation(Long seq);
    List<ReservationDTO> getReservations(String chartNumber, String reservationDate);
}

