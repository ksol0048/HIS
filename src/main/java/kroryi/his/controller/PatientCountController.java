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
                                  PatientAdmissionService patientAdmissionService, PatientAdmissionRepository patientAdmissionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.patientAdmissionService = patientAdmissionService;
        this.patientAdmissionRepository = patientAdmissionRepository;
    }


    @GetMapping("/completeTreatment/{number}/{date}")
    public ResponseEntity<Long> completeTreatment(@PathVariable String number, @PathVariable String date) {
        LocalDate localDate = LocalDate.parse(date); // yyyy-MM-dd 형식으로 입력

        long count = patientAdmissionService.getCompleteTreatmentCount(number, localDate);
        return ResponseEntity.ok(count);
    }
}

