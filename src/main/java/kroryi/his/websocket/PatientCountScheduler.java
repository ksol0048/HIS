package kroryi.his.websocket;

import kroryi.his.service.PatientAdmissionService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class PatientCountScheduler {
    private final SimpMessagingTemplate messagingTemplate;
    private final PatientAdmissionService patientAdmissionService;

    public PatientCountScheduler(SimpMessagingTemplate messagingTemplate, PatientAdmissionService patientAdmissionService) {
        this.messagingTemplate = messagingTemplate;
        this.patientAdmissionService = patientAdmissionService;
    }

    @Scheduled(fixedRate = 5000) // 5초마다 실행
    public void sendPatientCounts() {

        LocalDate today = LocalDate.now();
        for (String status : Arrays.asList("1", "2", "3")) {
            long count = patientAdmissionService.getCompleteTreatmentCount(status, today);
            Map<String, Object> patientCountUpdate = new HashMap<>();
            patientCountUpdate.put("status", status);
            patientCountUpdate.put("count", count);

            messagingTemplate.convertAndSend("/topic/patientCount", patientCountUpdate);
        }
    }
}
