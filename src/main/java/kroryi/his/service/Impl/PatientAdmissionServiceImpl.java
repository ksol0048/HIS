package kroryi.his.service.Impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kroryi.his.domain.PatientAdmission;
import kroryi.his.dto.PatientAdmissionDTO;
import kroryi.his.repository.PatientAdmissionRepository;
import kroryi.his.service.PatientAdmissionService;
import kroryi.his.websocket.MessageRequest;
import kroryi.his.websocket.PatientAdmissionListener;
import kroryi.his.websocket.RedisPublisher;
import kroryi.his.websocket.RedisSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PatientAdmissionServiceImpl implements PatientAdmissionService {
    private static final Logger log = LoggerFactory.getLogger(PatientAdmissionServiceImpl.class);
    @Autowired
    private PatientAdmissionRepository patientAdmissionRepository;

    private static final String STATUS_WAITING = "1";
    private static final String STATUS_IN_TREATMENT = "2";
    private static final String STATUS_COMPLETED = "3";

    @Autowired
    private ChannelTopic topic;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisPublisher redisPublisher;

    @Autowired
    private ObjectMapper objectMapper;


    // 환자 상태 집계 및 Redis 메시지 전송 메서드
    private void sendPatientStatusUpdate() {
        // 현재 상태 카운트 집계
        int status1 = patientAdmissionRepository.countByTreatStatusAndTodayReception("1");
        int status2 = patientAdmissionRepository.countByTreatStatusAndTodayReception("2");
        int status3 = patientAdmissionRepository.countByTreatStatusAndTodayReception("3");

        // 메시지 발송
        MessageRequest messageRequest = new MessageRequest(status1, status2, status3);

        try {
            // JSON 문자열로 변환
            String message = objectMapper.writeValueAsString(messageRequest);
            // Redis에 메시지 발송 (예: "patientStatusUpdate" 주제로)
            redisTemplate.convertAndSend("patientStatusUpdate", message);
        } catch (JsonProcessingException e) {
            e.printStackTrace(); // 예외 처리 (로깅 등)
        }
    }


    @Transactional
    @Override
    public PatientAdmission savePatientAdmission(PatientAdmissionDTO patientAdmissionDTO) throws JsonProcessingException {
        // 새로운 PatientAdmission 엔티티 생성 및 데이터 설정
        PatientAdmission patientAdmission = new PatientAdmission();
        patientAdmission.setChartNum(patientAdmissionDTO.getChartNum());
        patientAdmission.setPaName(patientAdmissionDTO.getPaName());
        patientAdmission.setMainDoc(patientAdmissionDTO.getMainDoc());
        patientAdmission.setReceptionTime(patientAdmissionDTO.getReceptionTime());
        patientAdmission.setRvTime(patientAdmissionDTO.getRvTime());
        patientAdmission.setViTime(patientAdmissionDTO.getViTime());
        patientAdmission.setCompletionTime(patientAdmissionDTO.getCompletionTime());
        patientAdmission.setTreatStatus(patientAdmissionDTO.getTreatStatus());

        // ID가 null인지 확인
        if (patientAdmission.getPid() != null) {
            throw new IllegalArgumentException("ID는 null이 아니어야 합니다."); // 예외 처리
        }

        // PatientAdmission 저장
        PatientAdmission savedAdmission = patientAdmissionRepository.save(patientAdmission);

        // 환자 상태 집계 및 Redis 메시지 전송
        sendPatientStatusUpdate();

        return savedAdmission;
    }



    @Override
    public List<PatientAdmission> getWaitingPatients() {
        return patientAdmissionRepository.findByTreatStatus("1");
    }

    @Override
    public List<PatientAdmission> getTreatmentPatients() {
        return patientAdmissionRepository.findByTreatStatus("2");
    }

    @Override
    public List<PatientAdmission> getCompletePatients() {
        return patientAdmissionRepository.findByTreatStatus("3");
    }


    @Transactional
    @Override
    public long getCompleteTreatmentCount(String count, LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay(); // 입력된 날짜의 00시 00분 00초
        LocalDateTime endDate = date.atTime(23, 59, 59);
        try {
            // 환자 정보 처리
            return patientAdmissionRepository.countByTreatStatusAndReceptionTimeBetween(count, startDate, endDate);
        } catch (Exception e) {
            // 예외 처리 시 세션 플러시 방지
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new RuntimeException("처리 중 오류 발생", e);
        }
    }

    @Override
    public List<PatientAdmission> findByChartNumAndReceptionTime(Integer chartNum, LocalDateTime receptionTime) {
        // receptionTime에서 날짜만 추출하여 LocalDate로 변환
        LocalDate receptionDate = LocalDate.from(receptionTime);

        // 차트 번호와 날짜를 기준으로 환자 리스트 조회
        List<PatientAdmission> patientAdmissions = patientAdmissionRepository.findByChartNumAndReceptionDate(chartNum, receptionDate);

        // 중복된 환자 정보를 처리하여 필요한 경우에 맞게 리턴
        return patientAdmissions; // 여기에 추가적인 로직을 넣어도 됩니다. (예: 가장 최근 환자만 선택)
    }


    @Override
    public void updatePatientAdmission(PatientAdmission patientAdmission) {
        // 상태 업데이트
        patientAdmissionRepository.save(patientAdmission);

        // 환자 상태 집계 및 Redis 메시지 전송
        sendPatientStatusUpdate();
    }


    @Override
    @Transactional(readOnly = true)
    public List<PatientAdmissionDTO> getAdmissionsByReceptionTime(LocalDateTime startDate, LocalDateTime endDate) {
        // 시작 시간과 종료 시간을 사용하여 데이터베이스에서 환자 접수 정보 조회
        List<PatientAdmission> admissions = patientAdmissionRepository.findByReceptionTimeBetween(startDate, endDate);

        // PatientAdmission 엔티티를 PatientAdmissionDTO로 변환
        return admissions.stream()
                .map(this::convertToDTO)
                .toList();
    }
    private PatientAdmissionDTO convertToDTO(PatientAdmission admission) {
        PatientAdmissionDTO dto = new PatientAdmissionDTO();
        dto.setChartNum(admission.getChartNum());
        dto.setPaName(admission.getPaName());
        dto.setMainDoc(admission.getMainDoc());
        dto.setReceptionTime(admission.getReceptionTime());
        dto.setRvTime(admission.getRvTime());
        dto.setTreatStatus(admission.getTreatStatus());
        dto.setViTime(admission.getViTime());
        dto.setCompletionTime(admission.getCompletionTime());
        return dto;
    }


    @Override
    @Transactional
    public void cancelAdmission(Integer pid) {
        // 환자 삭제 로직
        if (!patientAdmissionRepository.existsById(pid)) {
            throw new RuntimeException("환자를 찾을 수 없습니다."); // 예외 처리
        }

        // 환자 삭제
        patientAdmissionRepository.deleteById(pid);

        // 환자 상태 집계 및 Redis 메시지 전송
        sendPatientStatusUpdate();

        // Redis에 환자 삭제 메시지 발송
        redisTemplate.convertAndSend("/topic/admission/", "환자 삭제: " + pid);
    }



    @Override
    public PatientAdmission getLatestCompletionTime(Integer chartNum) {
        PatientAdmission admissions = patientAdmissionRepository.findLatestByChartNum(chartNum);
        log.info(admissions != null ? admissions.toString() : "chartNum {}에 대한 환자 데이터가 없습니다.", chartNum);
        return admissions; // 데이터가 없으면 null을 반환
    }

    @Override
    public void registerAdmission(PatientAdmission admission) {
        redisTemplate.convertAndSend("admission", "새로운 입원 등록: " + admission.getPaName());

    }

    @Override
    public void sendPatientCounts() {
        int waitingCount = patientAdmissionRepository.countByTreatStatusAndTodayReception(STATUS_WAITING);
        int inTreatmentCount = patientAdmissionRepository.countByTreatStatusAndTodayReception(STATUS_IN_TREATMENT);
        int completedCount = patientAdmissionRepository.countByTreatStatusAndTodayReception(STATUS_COMPLETED);

        // 메시지 객체 생성
        Map<String, Object> message = new HashMap<>();
        message.put("waitingCount", waitingCount);
        message.put("inTreatmentCount", inTreatmentCount);
        message.put("completedCount", completedCount);

        // WebSocket으로 환자 수 전송
        redisTemplate.convertAndSend("/pub/admission", message);
    }

    @Override
    public long getPatientCountByDateAndStatus(String date, String status) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 선택한 날짜의 시작과 끝을 계산
        Date startDate = dateFormat.parse(date);
        Date endDate = new Date(startDate.getTime() + (1000 * 60 * 60 * 24));  // 하루를 더해서 다음 날 자정

        // 레파지토리 호출
        return patientAdmissionRepository.countPatientsByDateAndStatus(startDate, endDate, status);
    }

    @Override
    public List<PatientAdmission> getWaitingPatientsForToday(LocalDate today) {
        // LocalDate를 LocalDateTime으로 변환 (오늘 시작과 끝 시간)
        LocalDateTime todayStart = today.atStartOfDay(); // 00:00:00
        LocalDateTime todayEnd = today.atTime(23, 59, 59); // 23:59:59

        // 변환된 LocalDateTime을 사용하여 쿼리 실행
        return patientAdmissionRepository.findByTreatStatusAndReceptionTimeIsToday("1", todayStart, todayEnd);
    }

    @Override
    public List<PatientAdmission> getTreatmentPatientsForToday(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay(); // 00:00:00
        LocalDateTime todayEnd = today.atTime(23, 59, 59); // 23:59:59

        // 변환된 LocalDateTime을 사용하여 쿼리 실행
        return patientAdmissionRepository.findByTreatStatusAndReceptionTimeIsToday("2", todayStart, todayEnd);
    }

    @Override
    public List<PatientAdmission> getCompletePatientsForToday(LocalDate today) {
        LocalDateTime todayStart = today.atStartOfDay(); // 00:00:00
        LocalDateTime todayEnd = today.atTime(23, 59, 59); // 23:59:59

        // 변환된 LocalDateTime을 사용하여 쿼리 실행
        return patientAdmissionRepository.findByTreatStatusAndReceptionTimeIsToday("3", todayStart, todayEnd);
    }


}