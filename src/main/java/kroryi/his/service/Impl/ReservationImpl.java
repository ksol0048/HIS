package kroryi.his.service.Impl;

import kroryi.his.dto.ReservationDTO;
import kroryi.his.service.ReservationService;

import java.util.List;

public class ReservationImpl implements ReservationService {
    @Override
    public List selectedDatePatientList(ReservationDTO dto) {
        return List.of();
    }

    @Override
    public List<ReservationDTO> selectedByReservation(ReservationDTO dto) {
        return List.of();
    }

    @Override
    public void insertReservationInformation(ReservationDTO dto) {

    }

    @Override
    public void updateReservationInformation(ReservationDTO dto) {

    }
}
