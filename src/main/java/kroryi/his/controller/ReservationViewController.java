package kroryi.his.controller;

import kroryi.his.dto.ReservationDTO;
import kroryi.his.service.PatientRegisterService;
import kroryi.his.service.ReservationRegisterService;
import kroryi.his.service.ReservationViewService;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*") // 모든 출처를 허용 (보안상 좋지 않을 수 있으니 주의)
@RestController
@RequestMapping("/reservation")
@Log4j2
public class ReservationViewController {

    // 로그 생성
    private static final Logger logger = LoggerFactory.getLogger(ReservationViewController.class);

    @Autowired
    private ReservationRegisterService rServ;

    @Autowired
    private ReservationViewService reservationViewService;

    @Autowired
    private PatientRegisterService patientRegisterService;

    // 캘린더에서 날짜 선택시
    @PostMapping("/selectedPatientDateList")
    public List<ReservationDTO> selectedDatePatientList(@RequestBody ReservationDTO dto) {

        // 클라이언트로부터 받은 날짜를 기준으로 데이터 조회 후 해당 데이터 리턴
        List<ReservationDTO> list = rServ.selectedDatePatientList(dto);

        return list;
    }


    // 모든 의사 가져오기
    @GetMapping("/doctorTimetable")
    public List<String> getDoctorNames() {
        List<String> doctorNames = patientRegisterService.getDoctorNames();
        log.info("Available Doctors: {}", doctorNames); // 의사 이름 로그 출력
        return doctorNames; // JSON 응답 반환
    }
}

