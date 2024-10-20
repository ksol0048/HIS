package kroryi.his.controller;

import java.util.List;

import ch.qos.logback.core.CoreConstants;
import groovy.lang.Lazy;
import kroryi.his.dto.ReservationDTO;
import kroryi.his.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*") // 모든 출처를 허용 (보안상 좋지 않을 수 있으니 주의)
@RestController
@RequestMapping("/api/main/")
public class ReservationController {

    // 로그 생성
    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService rServ;

    // 캘린더에서 날짜 선택시
    @PostMapping("/selectedDatePatientList")
    public List<ReservationDTO> selectedDatePatientList(@RequestBody ReservationDTO dto) {

        // 클라이언트로부터 받은 날짜를 기준으로 데이터 조회 후 해당 데이터 리턴
        List<ReservationDTO> list = rServ.selectedDatePatientList(dto);

        return list;
    }

    // 환자의 예약 정보 확인
    @PostMapping("/selectedByReservation")
    public List<ReservationDTO> selectedByReservation(@RequestBody ReservationDTO dto) {

        System.out.println(dto);
        // 클라이언트로부터 인덱스 번호를 기준으로 데이터 조회 후 해당 데이터 리턴
        List<ReservationDTO> list = rServ.selectedByReservation(dto);

        return list;
    }

    // 예약에서 저장을 눌렀을 경우
    @PostMapping("/insertReservationInformation")
    public ResponseEntity<String> insertReservationInformation(@RequestBody ReservationDTO dto) {
        try {
            // 해당 정보 저장
            rServ.insertReservationInformation(dto);
            return ResponseEntity.ok().build();
        }

        catch (Exception e) {
            // 예외가 발생한 경우 처리
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    // 예약에서 업데이트 되는 경우
    @PostMapping("/updateReservationInformation")
    public ResponseEntity<String> updateReservationInformation(@RequestBody ReservationDTO dto) {
        try {
            // 해당 정보 저장
            rServ.updateReservationInformation(dto);
            return ResponseEntity.ok().build();
        }

        catch (Exception e) {
            // 예외가 발생한 경우 처리
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/deleteReservation")
    public ResponseEntity<String> deleteReservation(@RequestParam Long seq) {
        System.out.println(seq);
        rServ.deleteReservation(seq);
        return ResponseEntity.ok("예약이 성공적으로 삭제되었습니다.");
    }

}

