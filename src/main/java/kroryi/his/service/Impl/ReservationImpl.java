package kroryi.his.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import kroryi.his.domain.Reservation;
import kroryi.his.dto.ReservationDTO;
import kroryi.his.mapper.ReservationMapper;
import kroryi.his.repository.ReservationRepository;
import kroryi.his.service.ReservationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationImpl implements ReservationService {

    private final ModelMapper modelMapper;

    @Autowired
    private ReservationMapper reMapper;

    @Autowired
    private ReservationRepository reRepo;

    @Autowired
    public ReservationImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    // 캘린더에서 날짜 선택시
    public List<ReservationDTO> selectedDatePatientList(ReservationDTO dto) {
        List<Reservation> reservations = reRepo.findByReservationDate(dto.getReservationDate());
        return reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationDTO.class))
                .collect(Collectors.toList());
    }

    // 환자의 예약 정보 확인
    public List<ReservationDTO> selectedByReservation(ReservationDTO dto) {
        List<Reservation> reservations = reRepo.findBySeq(dto.getSeq());
        return reservations.stream()
                .map(reservation -> modelMapper.map(reservation, ReservationDTO.class))
                .collect(Collectors.toList());
    }

    // 예약에서 저장을 눌렀을 경우
    public void insertReservationInformation(ReservationDTO dto) {
        Reservation reservation = modelMapper.map(dto, Reservation.class);
        System.out.println(reservation.getReservationDate());
        reRepo.save(reservation);
    }

    // 예약에서 수정을 눌렀을 경우
    public void updateReservationInformation(ReservationDTO dto) {
        Optional<Reservation> optionalReservation = reRepo.findById(dto.getSeq());

        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            modelMapper.map(dto, reservation); // DTO의 값을 기존 엔티티에 맵핑
            reRepo.save(reservation);
        } else {
            throw new EntityNotFoundException("Reservation not found with seq: " + dto.getSeq());
        }
    }
}

