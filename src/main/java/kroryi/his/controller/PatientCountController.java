package kroryi.his.controller;


import kroryi.his.domain.PatientAdmission;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.service.PatientAdmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class PatientCountController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PatientAdmissionService patientAdmissionService;
    private final PatientAdmissionRepository patientAdmissionRepository;

    public PatientCountController(SimpMessagingTemplate messagingTemplate,
                                  PatientAdmissionService patientAdmissionService,
                                  PatientAdmissionRepository patientAdmissionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.patientAdmissionService = patientAdmissionService;
        this.patientAdmissionRepository = patientAdmissionRepository;
    }

    @GetMapping("/completeTreatment/{number}/{date}")
    public ResponseEntity<Long> completeTreatment(@PathVariable String number, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date);
        long count = patientAdmissionService.getCompleteTreatmentCount(number, localDate);

        // 상태와 날짜에 따른 업데이트가 있을 때 클라이언트에 메시지를 전송
        sendPatientCountUpdate(number, count);

        return ResponseEntity.ok(count);
    }

    // 웹소켓을 통해 실시간 업데이트 전송
    public void sendPatientCountUpdate(String status, long count) {

        Map<String, Object> patientCountUpdate = new HashMap<>();
        patientCountUpdate.put("status", status);
        patientCountUpdate.put("count", count);
//        System.out.println("Sending update - Status: " + status + ", Count: " + count);

        messagingTemplate.convertAndSend("/topic/patientCount", patientCountUpdate);
    }
}

