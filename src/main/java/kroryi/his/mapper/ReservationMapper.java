package kroryi.his.mapper;


import kroryi.his.domain.Reservation;
import kroryi.his.dto.ReservationDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationMapper extends GenericMapper<ReservationDTO, Reservation>{

}
