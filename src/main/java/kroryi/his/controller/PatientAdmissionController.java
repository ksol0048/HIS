package kroryi.his.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import kroryi.his.domain.PatientAdmission;
import kroryi.his.domain.Reservation;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.dto.PatientDTO;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.repository.ReservationRepository;
import kroryi.his.service.PatientAdmissionService;
import kroryi.his.websocket.MessageRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Log4j2
@RestController
@RequestMapping("/api/patient-admission")
public class PatientAdmissionController {

    @Autowired
    private PatientAdmissionService patientAdmissionService;
    @Autowired
    private PatientAdmissionRepository patientAdmissionRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;



    // 환자 등록
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerPatient(@RequestBody PatientAdmissionDTO patientAdmissionDTO) throws JsonProcessingException {
        System.out.println("환자 등록 요청 수신: " + patientAdmissionDTO);
        Map<String, Object> response = new HashMap<>();

        // 차트 번호로 오늘 날짜 예약 정보 가져오기
        String chartNumber = String.valueOf(patientAdmissionDTO.getChartNum());
        List<Reservation> reservations = reservationRepository.findTodayReservationsByChartNumber(chartNumber);

        // 예약 정보가 존재하면 rvTime(예약 날짜)을 설정
        if (!reservations.isEmpty()) {
            // 예약 정보가 있다면 첫 번째 예약의 날짜를 설정 (예약이 하나라고 가정)
            String reservationDateString = reservations.get(0).getReservationDate(); // "2024-10-21T10:30" 형식
            System.out.println("예약 날짜 문자열: " + reservationDateString);
            try {
                // 문자열을 LocalDateTime으로 변환
                LocalDateTime reservationDateTime = LocalDateTime.parse(reservationDateString);

                // 예약 시간이 오늘이라면 rvTime을 설정
                patientAdmissionDTO.setRvTime(reservationDateTime); // 예약 시간이 오늘이라면 설정
                System.out.println("설정된 예약 시간: " + reservationDateTime);
            } catch (DateTimeParseException e) {
                response.put("message", "예약 날짜 형식이 잘못되었습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            patientAdmissionDTO.setRvTime(null); // 예약이 없거나 오늘 예약이 아닐 경우 null로 설정
            System.out.println("예약이 없거나 오늘 예약이 아닙니다.");
        }

        // 현재 시간을 접수 시간으로 설정
        patientAdmissionDTO.setReceptionTime(LocalDateTime.now());
        patientAdmissionDTO.setTreatStatus("1"); // 대기 상태는 1

        // DB에 저장
        PatientAdmission savedPatientAdmission = patientAdmissionService.savePatientAdmission(patientAdmissionDTO);

        System.out.println("저장된 환자 ID (pid): " + savedPatientAdmission.getPid());
        System.out.println("저장된 예약 시간 (rvTime): " + savedPatientAdmission.getRvTime());

        messagingTemplate.convertAndSend("/topic/waitingPatients", savedPatientAdmission);

        // 저장된 객체에서 pid와 rvTime을 가져와 응답에 포함
        response.put("data", savedPatientAdmission);
        response.put("message", "환자가 대기 상태로 등록되었습니다.");
        response.put("rvTime", savedPatientAdmission.getRvTime());  // 저장된 객체에서 가져오기

        return ResponseEntity.ok(response);
    }




    @MessageMapping("/waitingPatients")
    @SendTo("/topic/waitingPatients")
    public PatientAdmissionDTO broadcastPatient(PatientAdmissionDTO patientAdmissionDTO) {
        return patientAdmissionDTO;
    }

    @MessageMapping("/inTreatmentPatients")
    @SendTo("/topic/inTreatmentPatients")
    public PatientAdmissionDTO broadcastInTreatmentPatient(PatientAdmissionDTO patientAdmissionDTO) {
        return patientAdmissionDTO;
    }

    @MessageMapping("/completePatients")
    @SendTo("/topic/completePatients")
    public PatientAdmissionDTO broadcastInCompletePatient(PatientAdmissionDTO patientAdmissionDTO) {
        return patientAdmissionDTO;
    }

    // 대기 환자 목록 반환
    @GetMapping("/waiting")
    public ResponseEntity<List<PatientAdmission>> getPatientsWaiting() {
        LocalDate today = LocalDate.now(); // 오늘 날짜 구하기

        // 오늘 날짜에 대기 상태인 환자 목록을 가져옵니다.
        List<PatientAdmission> waitingPatients = patientAdmissionService.getWaitingPatientsForToday(today);

        log.info("waiting---->{}", waitingPatients);

        return ResponseEntity.ok(waitingPatients);
    }

    @GetMapping("/treatment")
    public ResponseEntity<List<PatientAdmission>> getPatientsForTreatment() {
        LocalDate today = LocalDate.now(); // 오늘 날짜 구하기

        // 오늘 날짜에 진료 중 상태인 환자 목록을 가져옵니다.
        List<PatientAdmission> inTreatmentPatients = patientAdmissionService.getTreatmentPatientsForToday(today);

        log.info("getTreatmentPatientsForToday---->{}", inTreatmentPatients);

        return ResponseEntity.ok(inTreatmentPatients);
    }



    // 진료 중 대기 환자 목록 반환
    @GetMapping("/complete")
    public ResponseEntity<List<PatientAdmission>> getPatientsForComplete() {
        LocalDate today = LocalDate.now(); // 오늘 날짜 구하기

        List<PatientAdmission> inCompletePatients = patientAdmissionService.getTreatmentPatientsForToday(today);

        log.info("getCompletePatients---->{}", inCompletePatients);

        return ResponseEntity.ok(inCompletePatients );
    }




    // 진료 시작
    @PutMapping("/treatment/start")
    public ResponseEntity<Map<String, String>> startTreatment(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        LocalDateTime receptionDateTime = LocalDate.now().atStartOfDay();
        patientAdmissionDTO.setReceptionTime(receptionDateTime);

        // 동일한 차트 번호와 접수 시간을 가진 환자들 조회
        List<PatientAdmission> existingPatients = patientAdmissionService.findByChartNumAndReceptionTime(
                Integer.valueOf(String.valueOf(patientAdmissionDTO.getChartNum())), receptionDateTime);

        // 새로 접수된 환자 처리
        for (PatientAdmission patient : existingPatients) {
            // PID가 같은 환자만 처리
            if (patient.getPid().equals(patientAdmissionDTO.getPid()) && patient.getTreatStatus().equals("1")) {
                // 진료 시작 처리
                patient.setViTime(LocalDateTime.now());
                patient.setTreatStatus("2");
                patient.setMainDoc(patientAdmissionDTO.getMainDoc());

                // DB 업데이트 수행
                patientAdmissionService.updatePatientAdmission(patient);

                // WebSocket 메시지 전송
                messagingTemplate.convertAndSend("/topic/inTreatmentPatients", patientAdmissionDTO);

                Map<String, String> response = new HashMap<>();
                response.put("message", "환자의 진료 상태가 2로 업데이트되었습니다.");
                return ResponseEntity.ok(response);
            }
        }

        // 모든 환자가 이미 진료 중이거나 업데이트할 환자가 없을 경우
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "이미 진료 중이거나 업데이트할 환자가 없습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    //    진료완료
    @PutMapping("/completeTreatment")
    public ResponseEntity<Map<String, String>> completeTreatment(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        Map<String, String> response = new HashMap<>(); // 응답 메시지를 위한 Map 생성

        // 진료 완료 시간 처리
        LocalDateTime completionTime = LocalDateTime.now();
        patientAdmissionDTO.setCompletionTime(completionTime); // completionTime 설정
        patientAdmissionDTO.setTreatStatus("3"); // 치료 상태를 "3"으로 설정

        // viTime 처리
        LocalDateTime viTime = patientAdmissionDTO.getViTime(); // 클라이언트로부터 받은 viTime
        if (viTime == null) {
            System.err.println("viTime이 null입니다.");
            response.put("message", "viTime은 필수입니다.");
            return ResponseEntity.badRequest().body(response);
        }

        // 동일한 차트 번호와 접수 시간을 가진 환자들 조회
        List<PatientAdmission> existingPatients = patientAdmissionService.findByChartNumAndReceptionTime(
                Integer.valueOf(String.valueOf(patientAdmissionDTO.getChartNum())),
                patientAdmissionDTO.getReceptionTime().toLocalDate().atStartOfDay());

        // 확인용 로그 추가: existingPatients가 비어있는지 확인
        System.out.println("찾은 환자 수: " + existingPatients.size());
        if (existingPatients.isEmpty()) {
            response.put("message", "해당하는 환자가 없습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 상태가 2인 환자만 진료 완료 처리
        for (PatientAdmission patient : existingPatients) {
            if (patient.getTreatStatus().equals("2")) { // TreatStatus가 "2"인 환자만 진료 완료 처리
                patient.setTreatStatus("3"); // 치료 상태를 "3"으로 설정
                patient.setCompletionTime(completionTime); // 진료 완료 시간 설정

                // DB에 업데이트 수행
                patientAdmissionService.updatePatientAdmission(patient);

                // WebSocket 메시지 전송
                messagingTemplate.convertAndSend("/topic/inCompletePatients", patientAdmissionDTO);

                response.put("message", "환자가 진료 완료 상태로 등록되었습니다.");
                return ResponseEntity.ok(response);
            }
        }

        // 상태가 2인 환자가 없을 경우
        response.put("message", "이미 진료 완료 상태이거나 업데이트할 환자가 없습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }






    @GetMapping("/date/{date}")
    public List<PatientAdmissionDTO> getAdmissionsByDate(@PathVariable String date) {
        try {
            // 날짜 문자열을 LocalDate로 변환
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime startDate = localDate.atStartOfDay(); // 시작 시간
            LocalDateTime endDate = localDate.plusDays(1).atStartOfDay(); // 끝 시간

//            System.out.println("시작 날짜: " + startDate); // 시작 날짜 로그
//            System.out.println("종료 날짜: " + endDate); // 종료 날짜 로그

            // 해당 날짜의 환자 접수 정보를 가져옴
            List<PatientAdmissionDTO> admissions = patientAdmissionService.getAdmissionsByReceptionTime(startDate, endDate);

            System.out.println("가져온 환자 접수 정보: " + admissions); // 가져온 환자 접수 정보 로그

            return admissions;
        } catch (Exception e) {
            e.printStackTrace(); // 예외 발생 시 스택 트레이스를 출력
            return Collections.emptyList(); // 비어 있는 리스트 반환
        }
    }

    @DeleteMapping("/{pid}")
    public ResponseEntity<Void> cancelAdmission(@PathVariable Integer pid) {
        try {
            patientAdmissionService.cancelAdmission(pid);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
    @GetMapping("/status")
    public ResponseEntity<MessageRequest> getPatientStatus() {
        int status1 = patientAdmissionRepository.countByTreatStatusAndTodayReception("1");
        int status2 = patientAdmissionRepository.countByTreatStatusAndTodayReception("2");
        int status3 = patientAdmissionRepository.countByTreatStatusAndTodayReception("3");
        MessageRequest messageRequest = new MessageRequest(status1, status2, status3);
        return ResponseEntity.ok(messageRequest);
    }

    @GetMapping("/count/{date}/{status}")
    public ResponseEntity<Long> getPatientCount(@PathVariable String date, @PathVariable String status) {
        try {
            // 날짜 형식이 맞는지 체크
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // 엄격한 날짜 검증

            // 날짜가 올바른 형식이 아니라면 예외 발생
            sdf.parse(date);

            long count = patientAdmissionService.getPatientCountByDateAndStatus(date, status);
            return ResponseEntity.ok(count);
        } catch (ParseException e) {
            // 날짜 형식 오류 처리
            return ResponseEntity.badRequest().body(0L); // 400 Bad Request 응답
        }
    }

}