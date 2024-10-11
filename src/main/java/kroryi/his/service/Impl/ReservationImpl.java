package kroryi.his.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import kroryi.his.domain.Reservation;
import kroryi.his.dto.ReservationDTO;
import kroryi.his.mapper.ReservationMapper;
import kroryi.his.repository.ReservationRepository;
import kroryi.his.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationImpl implements ReservationService {
    @Autowired
    private ReservationMapper reMapper;

    @Autowired
    private ReservationRepository reRepo;

    // 캘린더에서 날짜 선택시
    public List selectedDatePatientList(ReservationDTO dto) {

        // reservationDate에 해당하는 데이터를 조회
        List reservations = reRepo.findByReservationDate(dto.getReservationDate());

        // 엔티티를 DTO로 변환하여 반환
        return reMapper.toDtoList(reservations);
    }

    // 환자의 예약 정보 확인
    public List<ReservationDTO> selectedByReservation(ReservationDTO dto) {

        // reservationDate에 해당하는 데이터를 조회
        List<Reservation> reservations = reRepo.findBySeq(dto.getSeq());

        // 엔티티를 DTO로 변환하여 반환
        return reMapper.toDtoList(reservations);
    }

    // 예약에서 저장을 눌렀을 경우
    public void insertReservationInformation(ReservationDTO dto) {
        Reservation rList = reMapper.toEntity(dto);
        reRepo.save(rList);
    }

    // 예약에서 수정을 눌렀을 경우
    public void updateReservationInformation(ReservationDTO dto) {
        // seq를 이용해 기존 예약 정보 조회
        Optional<Reservation> optionalReservation = reRepo.findById(dto.getSeq());

        if (optionalReservation.isPresent()) {
            // 기존 예약 정보 가져오기
            Reservation reservation = optionalReservation.get();

            // 필요한 필드 업데이트
            reservation.setReservationDate(dto.getReservationDate());
            reservation.setDepartment(dto.getDepartment());
            reservation.setPatientNote(dto.getPatientNote());
            // 추가적으로 업데이트할 필드가 있으면 여기에 작성
            // ...

            // 업데이트된 엔티티 저장
            reRepo.save(reservation);
        } else {
            throw new EntityNotFoundException("Reservation not found with seq: " + dto.getSeq());
        }
    }
}