package kroryi.his.controller;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.domain.Reservation;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.ReservationRepository;
import kroryi.his.service.PatientAdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient-admission")
public class PatientAdmissionController {

    @Autowired
    private PatientAdmissionService patientAdmissionService;

    @Autowired
    private ReservationRepository reservationRepository;

    // 환자 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerPatient(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        System.out.println("환자 등록 요청 수신: " + patientAdmissionDTO);

        // 차트 번호로 예약 정보를 가져오기
        Optional<Reservation> reservation = reservationRepository.findByChartNumber(String.valueOf(patientAdmissionDTO.getChartNum()));

        // 예약 정보가 존재하면 rvTime(예약 날짜)을 설정
        if (reservation.isPresent()) {
            String reservationDateString = reservation.get().getReservationDate(); // "2024-10-21T10:30" 형식

            try {
                // 문자열을 LocalDateTime으로 변환
                LocalDateTime reservationDateTime = LocalDateTime.parse(reservationDateString);

                // 오늘 날짜와 예약 날짜 비교
                LocalDate today = LocalDate.now();
                if (reservationDateTime.toLocalDate().isEqual(today)) {
                    patientAdmissionDTO.setRvTime(reservationDateTime); // 예약 시간이 오늘이라면 설정
                } else {
                    return ResponseEntity.badRequest().body("{\"message\": \"예약 날짜가 오늘이 아닙니다.\"}");
                }
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body("{\"message\": \"예약 날짜 형식이 잘못되었습니다.\"}");
            }
        } else {
            return ResponseEntity.badRequest().body("{\"message\": \"예약 정보가 없습니다.\"}"); // 예약 정보가 없을 경우 응답
        }

        // 현재 시간을 접수 시간으로 설정
        patientAdmissionDTO.setReceptionTime(LocalDateTime.now());
        patientAdmissionDTO.setTreatStatus("1"); // 대기 상태는 1

        // DB에 저장
        patientAdmissionService.savePatientAdmission(patientAdmissionDTO);

        return ResponseEntity.ok("{\"message\": \"환자가 대기 상태로 등록되었습니다.\", \"rvTime\": \"" + patientAdmissionDTO.getRvTime() + "\"}");
    }



    // 대기 환자 목록 반환
    @GetMapping("/waiting")
    public ResponseEntity<List<PatientAdmission>> getWaitingPatients() {
        List<PatientAdmission> waitingPatients = patientAdmissionService.getWaitingPatients();
        return ResponseEntity.ok(waitingPatients);
    }

    // 진료 시작
    @PostMapping("/treatment/start")
    public ResponseEntity<String> startTreatment(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        System.out.println("진료 시작 요청 수신: " + patientAdmissionDTO);


        patientAdmissionDTO.setViTime(LocalDateTime.now());
        patientAdmissionDTO.setTreatStatus("2"); // 진료중은 2

        // DB에 저장
        patientAdmissionService.savePatientAdmission(patientAdmissionDTO);

        return ResponseEntity.ok("환자가 진료 중 상태로 등록되었습니다.");
    }

    // 진료 중 대기 환자 목록 반환
    @GetMapping("/treatment/waiting")
    public ResponseEntity<List<PatientAdmission>> getWaitingPatientsForTreatment() {
        List<PatientAdmission> inTreatmentPatients  = patientAdmissionService.getWaitingPatients();
        return ResponseEntity.ok(inTreatmentPatients );
    }

    //    진료완료
    @PostMapping("/completeTreatment")
    public ResponseEntity<String> completed(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        System.out.println("진료 완료 요청 수신: " + patientAdmissionDTO);

        // 진료 완료 시간 처리
        LocalDateTime completionTime = LocalDateTime.now();
        patientAdmissionDTO.setCompletionTime(completionTime); // completionTime 설정
        patientAdmissionDTO.setTreatStatus("3");

        // viTime 처리
        LocalDateTime viTime = patientAdmissionDTO.getViTime(); // 클라이언트로부터 받은 viTime
        if (viTime == null) {
            System.err.println("viTime이 null입니다.");
            return ResponseEntity.badRequest().body("{\"message\": \"viTime은 필수입니다.\"}");
        }

        // DTO 저장
        patientAdmissionService.savePatientAdmission(patientAdmissionDTO);
        return ResponseEntity.ok("{\"message\": \"환자가 진료 완료 상태로 등록되었습니다.\"}");
    }




    @GetMapping("/completeTreatment/{number}/{date}")
    public ResponseEntity<Long> completeTreatment(@PathVariable String number, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date); // yyyy-MM-dd 형식으로 입력

        long count = patientAdmissionService.getCompleteTreatmentCount(number, localDate);
        return ResponseEntity.ok(count);
    }

//    @PutMapping("/treatment/update/{chartNum}")
//    public ResponseEntity<String> updateTreatment(@PathVariable Integer chartNum, @RequestBody PatientAdmissionDTO patientAdmissionDTO) {
//        try {
//            // 오늘 날짜의 시작과 끝 시간을 계산
//            LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN); // 오늘의 00:00:00
//            LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);   // 오늘의 23:59:59
//
//            // 차트 번호와 오늘의 receptionTime을 기준으로 환자 정보 업데이트
//            boolean updated = patientAdmissionService.updatePatientRecord(chartNum, patientAdmissionDTO, startOfToday, endOfToday);
//
//            if (updated) {
//                return ResponseEntity.ok("환자 정보가 성공적으로 업데이트되었습니다.");
//            } else {
//                return ResponseEntity.status(404).body("해당 조건에 맞는 환자를 찾을 수 없습니다.");
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("환자 정보 업데이트 중 오류가 발생했습니다: " + e.getMessage());
//        }
//    }


}
