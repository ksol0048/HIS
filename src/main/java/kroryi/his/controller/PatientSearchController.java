package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import kroryi.his.domain.PatientAdmission;
import kroryi.his.domain.PatientRegister;
import kroryi.his.dto.SearchKeywordDTO;
import kroryi.his.service.Impl.PatientRegisterServiceImpl;
import kroryi.his.service.PatientAdmissionService;
import kroryi.his.service.PatientRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient_search")
@Log4j2
@RequiredArgsConstructor
public class PatientSearchController {
    private final PatientRegisterService patientRegisterService;

    private final PatientAdmissionService patientAdmissionService;

    @ApiOperation(value = "keyword 검색 POST", notes = "POST 방식으로 환자 검색")
    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> searchName(@RequestBody SearchKeywordDTO keyword) {
        log.info("keyword: {}", keyword);
        Map<String,Object> result = new HashMap<>();

        // 키워드로 환자 검색
        List<PatientRegister> registers = patientRegisterService.searchNameByKeyword(keyword.getKeyword());

        for (PatientRegister register : registers) {
            log.info(register.toString());
        }

        result.put("result", registers);
        return result;
    }

    @ApiOperation(value = "환자검색 POST", notes = "POST 방식으로 환자 정보")
    @PostMapping(value = "/{chartNum}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String,Object> searchChartNum(@PathVariable String chartNum) {
        log.info("chartNum: {}", chartNum);
        Map<String,Object> result = new HashMap<>();

        // 키워드로 환자 검색
        PatientRegister register = patientRegisterService.getPatient(chartNum);

            log.info(register.toString());

        result.put("result", register);
        return result;
    }

    @ApiOperation(value = "최종내원 POST", notes = "POST 방식으로 최종내원")
    @PostMapping(value = "/patient_LastVisit/{chartNum}")
    public PatientAdmission searchLastVisit(@PathVariable Integer chartNum) {
        log.info("chartNum1111: {}", chartNum);
        PatientAdmission patientAdmission = patientAdmissionService.getLatestCompletionTime(chartNum);
log.info(patientAdmission);
        // 데이터가 없을 경우 기본 메시지를 포함한 PatientAdmission 인스턴스를 반환
        if (patientAdmission == null) {
            log.info("chartNum {}에 대한 환자 데이터가 없습니다.", chartNum);
            PatientAdmission emptyAdmission = new PatientAdmission();
            emptyAdmission.setMessage("최종내원일이 없습니다."); // 이 메서드가 존재하는지 확인
            return emptyAdmission;
        }

        log.info("!!!!!!!!!!!!!!!!!" + patientAdmission.toString());
        return patientAdmission;
    }

}
