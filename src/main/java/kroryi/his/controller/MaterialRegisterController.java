package kroryi.his.controller;


import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.domain.CompanyRegister;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;
import kroryi.his.service.CompanyRegisterService;
import kroryi.his.service.MaterialRegisterService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialRegisterController {

    private final MaterialRegisterService materialRegisterService;
    private final CompanyRegisterService companyRegisterService;

    // 재료 등록 (POST 요청)
    @ApiOperation(value = "재료 등록", notes = "POST 방식으로 재료를 등록합니다.")
    @PostMapping(value = "/addMaterial", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerMaterial(@Valid @RequestBody MaterialDTO materialDTO, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            // 재료 등록
            MaterialRegister materialRegister = materialRegisterService.register(materialDTO);
            return ResponseEntity.ok(Map.of("success", true, "message", "재료가 등록되었습니다.", "materialRegister", materialRegister));
        } catch (IllegalArgumentException e) {
            // 중복된 경우 예외 처리
            log.warn("재료 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("재료 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }



    // 재료 수정 (PUT 요청)
    @ApiOperation(value = "재료 수정", notes = "PUT 방식으로 재료 정보를 수정합니다.")
    @PutMapping("/updateMaterial")
    public ResponseEntity<?> updateMaterial(@RequestBody MaterialDTO materialDTO) {
        boolean isNewMaterial = materialRegisterService.isNewMaterial(materialDTO.getMaterialCode());
        materialRegisterService.updateMaterial(materialDTO);

        if (isNewMaterial) {
            return ResponseEntity.ok(Map.of("success", true, "message", "재료가 등록되었습니다."));
        } else {
            return ResponseEntity.ok(Map.of("success", true, "message", "재료가 수정되었습니다."));
        }
    }

    // 재료 검색 (GET 요청)
    @ApiOperation(value = "재료 검색", notes = "GET 방식으로 재료를 검색합니다.")
    @GetMapping("/searchMaterial")
    public ResponseEntity<List<MaterialDTO>> searchMaterials(
            @RequestParam(value = "materialName", required = false) String materialName,
            @RequestParam(value = "companyName", required = false) String companyName) {

        try {
            List<MaterialRegister> materials;

            // 검색 조건에 따라 재료 리스트 반환
            if ((materialName == null || materialName.isEmpty()) && (companyName == null || companyName.isEmpty())) {
                materials = materialRegisterService.getAllMaterial();  // 검색 조건이 없으면 전체 리스트 반환
            } else if (materialName != null && !materialName.isEmpty() && companyName != null && !companyName.isEmpty()) {
                materials = materialRegisterService.searchByCompanyNameAndMaterialName(companyName, materialName);
            } else if (companyName != null && !companyName.isEmpty()) {
                materials = materialRegisterService.searchByCompanyName(companyName);
            } else {
                materials = materialRegisterService.searchByMaterialName(materialName);
            }

            List<MaterialDTO> result = materials.stream()
                    .map(MaterialDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("재료 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 재료 삭제 (DELETE 요청)
    @ApiOperation(value = "재료 삭제", notes = "DELETE 방식으로 재료를 삭제합니다.")
    @DeleteMapping("/deleteMaterial")
    public ResponseEntity<String> deleteMaterial(@RequestParam String materialCode) {
        try {
            // 재료 삭제
            materialRegisterService.customDeleteByMaterialCode(materialCode);
            return ResponseEntity.ok("재료가 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("재료 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("재료 삭제에 실패했습니다.");
        }
    }

    // 업체 목록 조회 (GET 요청)
    @ApiOperation(value = "업체 목록 조회", notes = "GET 방식으로 업체 목록을 조회합니다.")
    @GetMapping("/searchCompanies")
    public ResponseEntity<List<CompanyRegister>> searchCompanies() {
        try {
            // 업체 목록 조회
            List<CompanyRegister> companies = companyRegisterService.getAllCompanies();
            return ResponseEntity.ok(companies);
        } catch (Exception e) {
            log.error("업체 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}

