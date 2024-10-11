package kroryi.his.mapper;


import kroryi.his.domain.Reservation;
import kroryi.his.dto.ReservationDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {
    ReservationDTO toDto(Reservation reservation);
    Reservation toEntity(ReservationDTO reservationDTO);
    List<ReservationDTO> toDtoList(List<Reservation> reservations);
}
