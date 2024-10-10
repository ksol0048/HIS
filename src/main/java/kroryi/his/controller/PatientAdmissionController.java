package kroryi.his.controller;

import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.service.PatientAdmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/patient-admission")
public class PatientAdmissionController {

    @Autowired
    private PatientAdmissionService patientAdmissionService;

    // 환자 등록
    @PostMapping("/register")
    public ResponseEntity<String> registerPatient(@RequestBody PatientAdmissionDTO patientAdmissionDTO) {
        System.out.println("환자 등록 요청 수신: " + patientAdmissionDTO);

        // 현재 시간을 접수 시간으로 설정
        patientAdmissionDTO.setReceptionTime(LocalDateTime.now());
        patientAdmissionDTO.setTreatStatus("1"); // 대기 상태는 1

        // DB에 저장
        patientAdmissionService.savePatientAdmission(patientAdmissionDTO);

        return ResponseEntity.ok("{\"message\": \"환자가 대기 상태로 등록되었습니다.\"}");
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

}
