package kroryi.his.controller;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.domain.Reservation;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.ReservationRepository;
import kroryi.his.service.PatientAdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/patient-admission")
public class PatientAdmissionController {

    @Autowired
    private PatientAdmissionService patientAdmissionService;

    @Autowired
    private ReservationRepository reservationRepository;

    // 환자 등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerPatient(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {

        System.out.println("환자 등록 요청 수신: " + patientAdmissionDTO);
        Map<String, Object> response = new HashMap<>();

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
                    response.put("message", "예약 날짜가 오늘이 아닙니다.");
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (DateTimeParseException e) {
                response.put("message", "예약 날짜가 오늘이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            response.put("message", "예약 날짜 형식이 잘못되었습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 현재 시간을 접수 시간으로 설정
        patientAdmissionDTO.setReceptionTime(LocalDateTime.now());
        patientAdmissionDTO.setTreatStatus("1"); // 대기 상태는 1

        // DB에 저장
        PatientAdmission patientAdmissionDTO1 = patientAdmissionService.savePatientAdmission(patientAdmissionDTO);


        response.put("data", patientAdmissionDTO1);
        response.put("message", "환자가 대기 상태로 등록되었습니다.");
        response.put("rvTime", patientAdmissionDTO.getRvTime());

        return ResponseEntity.ok(response);
    }



    // 대기 환자 목록 반환
    @GetMapping("/waiting")
    public ResponseEntity<List<PatientAdmission>> getWaitingPatients() {
        List<PatientAdmission> waitingPatients = patientAdmissionService.getWaitingPatients();
        return ResponseEntity.ok(waitingPatients);
    }

    // 진료 시작
    @PutMapping("/treatment/start")
    public ResponseEntity<String> startTreatment(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        // 오늘 날짜의 시작 시간을 구함 (예: 2024-10-23 00:00:00)
        LocalDateTime receptionDateTime = LocalDate.now().atStartOfDay();

        // DTO에 접수 시간 설정
        patientAdmissionDTO.setReceptionTime(receptionDateTime);

        // 차트 번호와 접수 시간이 일치하는 환자 조회
        Optional<PatientAdmission> existingPatient = patientAdmissionService.findByChartNumAndReceptionTime(
                patientAdmissionDTO.getChartNum(), receptionDateTime);

        System.out.println("조회된 환자: " + existingPatient);
        System.out.println("차트 번호: " + patientAdmissionDTO.getChartNum());
        System.out.println("접수 시간: " + receptionDateTime);

        if (existingPatient.isPresent()) {
            PatientAdmission patient = existingPatient.get();

            System.out.println("조회된 환자: 차트 번호: " + patient.getChartNum() + ", 진료 상태: " + patient.getTreatStatus());

            // 피아이디가 같고, 현재 진료 상태가 "1"인 경우
            if (patient.getTreatStatus().equals("1")) {
                patient.setViTime(LocalDateTime.now()); // 진료 시작 시간을 현재 시간으로 설정
                patient.setTreatStatus("2"); // 진료 상태를 "2"로 업데이트
                patient.setMainDoc(patientAdmissionDTO.getMainDoc()); // 의사 정보 업데이트

                // DB에 업데이트 수행 (여기서는 save가 아닌 update 메서드 사용)
                patientAdmissionService.updatePatientAdmission(patient); // 업데이트 로직을 구현해야 합니다.

                System.out.println("기존 환자 정보 업데이트: 차트 번호: " + patientAdmissionDTO.getChartNum() +
                        ", 새로운 진료 시간: " + patient.getViTime() +
                        ", 새로운 진료 상태: 2" +
                        ", 새로운 의사: " + patient.getMainDoc());

                return ResponseEntity.ok("환자의 진료 상태가 2로 업데이트되었습니다.");
            } else if (patient.getTreatStatus().equals("2")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("환자가 이미 진료 중입니다.");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("알 수 없는 오류가 발생했습니다.");
    }







    // 진료 중 대기 환자 목록 반환
    @GetMapping("/treatment/waiting")
    public ResponseEntity<List<PatientAdmission>> getWaitingPatientsForTreatment() {
        List<PatientAdmission> inTreatmentPatients  = patientAdmissionService.getWaitingPatients();
        return ResponseEntity.ok(inTreatmentPatients );
    }

    //    진료완료
    @PutMapping("/completeTreatment")
    public ResponseEntity<String> completeTreatment(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        System.out.println("진료 완료 요청 수신: " + patientAdmissionDTO);

        // 진료 완료 시간 처리
        LocalDateTime completionTime = LocalDateTime.now();
        patientAdmissionDTO.setCompletionTime(completionTime); // completionTime 설정
        patientAdmissionDTO.setTreatStatus("3"); // 치료 상태를 "3"으로 설정

        // viTime 처리
        LocalDateTime viTime = patientAdmissionDTO.getViTime(); // 클라이언트로부터 받은 viTime
        if (viTime == null) {
            System.err.println("viTime이 null입니다.");
            return ResponseEntity.badRequest().body("{\"message\": \"viTime은 필수입니다.\"}");
        }

        // 환자 정보 조회: 차트 번호와 접수 시간을 기준으로
        Optional<PatientAdmission> existingPatient = patientAdmissionService.findByChartNumAndReceptionTime(
                patientAdmissionDTO.getChartNum(), patientAdmissionDTO.getReceptionTime().toLocalDate().atStartOfDay());

        if (existingPatient.isPresent()) {
            PatientAdmission patient = existingPatient.get();

            // 치료 상태가 2인지 확인
            if (patient.getTreatStatus().equals("2")) {
                // 치료 상태를 3으로 업데이트
                patient.setTreatStatus("3");
                patient.setCompletionTime(completionTime); // 진료 완료 시간 설정

                patientAdmissionService.updatePatientAdmission(patient); // 업데이트 수행

                return ResponseEntity.ok("{\"message\": \"환자가 진료 완료 상태로 등록되었습니다.\"}");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"환자는 이미 진료 완료 상태입니다.\"}");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\": \"환자를 찾을 수 없습니다.\"}");
    }




    @GetMapping("/completeTreatment/{number}/{date}")
    public ResponseEntity<Long> completeTreatment(@PathVariable String number, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date); // yyyy-MM-dd 형식으로 입력

        long count = patientAdmissionService.getCompleteTreatmentCount(number, localDate);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/date/{date}")
    public List<PatientAdmissionDTO> getAdmissionsByDate(@PathVariable String date) {
        try {
            // 날짜 문자열을 LocalDate로 변환
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startDate = localDate.atStartOfDay(); // 시작 시간
            LocalDateTime endDate = localDate.plusDays(1).atStartOfDay(); // 끝 시간

            System.out.println("시작 날짜: " + startDate); // 시작 날짜 로그
            System.out.println("종료 날짜: " + endDate); // 종료 날짜 로그

            // 해당 날짜의 환자 접수 정보를 가져옴
            List<PatientAdmissionDTO> admissions = patientAdmissionService.getAdmissionsByReceptionTime(startDate, endDate);

            System.out.println("가져온 환자 접수 정보: " + admissions); // 가져온 환자 접수 정보 로그

            return admissions;
        } catch (Exception e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스를 출력
            return Collections.emptyList(); // 비어 있는 리스트 반환
        }
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
