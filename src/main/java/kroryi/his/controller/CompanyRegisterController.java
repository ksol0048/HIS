package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.domain.CompanyRegister;
import kroryi.his.dto.CompanyDTO;
import kroryi.his.service.CompanyRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory_management")
@Log4j2
@RequiredArgsConstructor
public class CompanyRegisterController {
    private final CompanyRegisterService companyRegisterService;

    // 업체 등록 (POST 요청)
    @ApiOperation(value = "업체 등록", notes = "POST 방식으로 새로운 업체를 등록합니다.")
    @PostMapping(value = "/addCompany", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> registerCompany(@Valid @RequestBody CompanyDTO companyDTO,
                                                               BindingResult bindingResult) throws BindException {
        log.info("CompanyDTO -> {}", companyDTO);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        Map<String, Object> result = new HashMap<>();

        try {
            if (companyRegisterService.isCompanyCodeDuplicate(companyDTO.getCompanyCode())) {
                throw new IllegalArgumentException("이미 등록된 업체입니다.");
            }

            CompanyRegister companyRegister = companyRegisterService.register(companyDTO);
            log.info("CompanyRegister -> {}", companyRegister);

            result.put("success", true);
            result.put("message", "업체가 성공적으로 등록되었습니다.");
            result.put("companyRegister", companyRegister);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("업체 등록 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        } catch (Exception e) {
            log.error("업체 등록 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "업체 등록 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 업체 수정 (PUT 요청)
    @ApiOperation(value = "업체 수정", notes = "PUT 방식으로 기존 업체 정보를 수정합니다.")
    @PutMapping(value = "/updateCompany", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateCompany(@Valid @RequestBody CompanyDTO companyDTO,
                                                             BindingResult bindingResult) throws BindException {
        log.info("CompanyDTO -> {}", companyDTO);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        Map<String, Object> result = new HashMap<>();

        try {
            // 업체가 존재하는지 확인 후 수정
            if (!companyRegisterService.isCompanyCodeDuplicate(companyDTO.getCompanyCode())) {
                throw new IllegalArgumentException("존재하지 않는 업체 코드입니다.");
            }

            CompanyRegister companyRegister = companyRegisterService.update(companyDTO);
            log.info("CompanyRegister -> {}", companyRegister);

            result.put("success", true);
            result.put("message", "업체가 성공적으로 수정되었습니다.");
            result.put("companyRegister", companyRegister);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("업체 수정 실패: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } catch (Exception e) {
            log.error("업체 수정 중 오류 발생", e);
            result.put("success", false);
            result.put("message", "업체 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/checkCompanyCode")
    public ResponseEntity<Boolean> checkCompanyCode(@RequestParam String companyCode) {
        boolean exists = companyRegisterService.isCompanyCodeDuplicate(companyCode);
        return ResponseEntity.ok(exists);
    }

    // 업체 검색
    @GetMapping("/searchCompany")
    public List<CompanyRegister> searchCompanies(@RequestParam(value = "companyName", required = false) String companyName) {
        if (companyName == null || companyName.isEmpty()) {
            return companyRegisterService.getAllCompanies();  // 검색어가 없을 때 전체 업체 반환
        }
        return companyRegisterService.searchByName(companyName);  // 검색어에 따른 업체 반환
    }


    // 업체 삭제
    @DeleteMapping("/deleteCompany")
    public ResponseEntity<String> deleteCompany(@RequestParam String companyCode) {
        companyRegisterService.deleteCompany(companyCode);
        return ResponseEntity.ok("업체가 성공적으로 삭제되었습니다.");
    }
}
