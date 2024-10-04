package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialRegisterService;
import kroryi.his.service.MaterialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
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
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialTransactionController {

    private final MaterialRegisterService materialRegisterService;
    private final MaterialTransactionService materialTransactionService;

    // 재료 목록 조회 (GET 요청)
    @ApiOperation(value = "재료 목록 조회", notes = "GET 방식으로 재료 목록을 조회합니다.")
    @GetMapping("/searchMaterials")
    public ResponseEntity<List<MaterialDTO>> searchMaterials() {
        try {
            List<MaterialDTO> materialCompanies = materialRegisterService.getAllMaterialsWithCompany();
            return ResponseEntity.ok(materialCompanies);
        } catch (Exception e) {
            log.error("재료 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 재료 출납 등록 (POST 요청)
    @ApiOperation(value = "재료 출납 등록", notes = "POST 방식으로 출납을 등록합니다.")
    @PostMapping(value = "/addTransaction", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@Valid @RequestBody MaterialTransactionDTO materialTransactionDTO, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            // 재료 등록
            MaterialTransactionRegister materialTransactionRegister = materialTransactionService.register(materialTransactionDTO);
            log.info("Material Code: {}", materialTransactionDTO.getMaterialCode());
            return ResponseEntity.ok(Map.of("success", true, "message", "재료가 등록되었습니다.", "materialTransactionRegister", materialTransactionRegister));
        } catch (IllegalArgumentException e) {
            // 중복된 경우 예외 처리
            log.warn("재료 출납 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("재료 출납 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 재료 출납 목록 조회
    @ApiOperation(value = "재료 출납 검색", notes = "GET 방식으로 출납을 검색합니다.")
    @GetMapping("/searchTransaction")
    public ResponseEntity<List<MaterialTransactionDTO>> searchTransaction(
            @RequestParam(value = "materialName", required = false) String materialName,
            @RequestParam(value = "materialCode", required = false) String materialCode) {

        List<MaterialTransactionDTO> transactions;

        if (materialName == null && materialCode == null) {
            transactions = materialTransactionService.getAllTransactions();
        } else if (materialName != null) {
            transactions = materialTransactionService.searchByMaterialName(materialName);
        } else {
            transactions = materialTransactionService.searchByMaterialCode(materialCode);
        }

        return ResponseEntity.ok(transactions);
    }

}