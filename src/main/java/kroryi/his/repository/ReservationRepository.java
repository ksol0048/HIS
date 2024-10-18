package kroryi.his.repository;

import java.util.List;

import kroryi.his.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>{

    // 특정 날짜에 해당하는 예약을 조회하는 메서드
    List<Reservation> findByReservationDate(String reservationDate);

    // 인덱스 번호에 해당하는 예약 정보를 조회하는 메서드
    List<Reservation> findBySeq(Long seq);

}
