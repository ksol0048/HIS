package kroryi.his.controller;

import io.swagger.annotations.ApiOperation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;
import kroryi.his.dto.MaterialStockOutDTO;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialRegisterService;
import kroryi.his.service.MaterialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    @PostMapping("/addTransaction")
    public ResponseEntity<?> addTransaction(@Valid @RequestBody MaterialTransactionDTO materialTransactionDTO, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            // 재료 출납 등록 로직
            MaterialTransactionRegister materialTransactionRegister = materialTransactionService.register(materialTransactionDTO);
            log.info("Transaction Registered for Material Code: {}", materialTransactionDTO.getMaterialCode());
            return ResponseEntity.ok(Map.of("success", true, "message", "재료 출납이 등록되었습니다.", "materialTransactionRegister", materialTransactionRegister));
        } catch (IllegalArgumentException e) {
            log.warn("재료 출납 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("재료 출납 등록 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 재료 출납 수정 (PUT 요청)
    @PutMapping("/updateTransaction")
    public ResponseEntity<?> updateTransaction(@Valid @RequestBody MaterialTransactionDTO materialTransactionDTO, BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        try {
            // 재료 출납 수정 로직
            MaterialTransactionRegister materialTransactionRegister = materialTransactionService.update(materialTransactionDTO);
            log.info("Transaction Updated for Material Code: {}", materialTransactionDTO.getMaterialCode());
            return ResponseEntity.ok(Map.of("success", true, "message", "재료 출납이 수정되었습니다.", "materialTransactionRegister", materialTransactionRegister));
        } catch (IllegalArgumentException e) {
            log.warn("재료 출납 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("재료 출납 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 재료 출납 삭제 (DELETE 요청)
    @DeleteMapping("/deleteTransaction/{transactionId}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long transactionId) {
        try {
            materialTransactionService.deleteByTransactionId(transactionId);
            log.info("Transaction Deleted for Transaction ID: {}", transactionId);
            return ResponseEntity.ok(Map.of("success", true, "message", "재료가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("재료 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 재료 조회
    @ApiOperation(value = "재료 조회", notes = "GET 방식으로 재료 조회를 합니다.")
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

    // 재료 출납 목록 조회
    @ApiOperation(value = "재료 출납 검색", notes = "입고일자, 재료명, 재료코드, 업체명 등으로 출납을 검색합니다.")
    @GetMapping("/findTransaction")
    public ResponseEntity<List<MaterialTransactionDTO>> searchTransaction(
            @RequestParam(value = "transactionStartDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionStartDate,
            @RequestParam(value = "transactionEndDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate transactionEndDate,
            @RequestParam(value = "materialName", required = false) String materialName,
            @RequestParam(value = "materialCode", required = false) String materialCode,
            @RequestParam(value = "companyName", required = false) String companyName,
            @RequestParam(value = "belowSafetyStock", required = false) Boolean belowSafetyStock,
            @RequestParam(value = "stockManagementItem", required = false) Boolean stockManagementItem) {

        try {
            // 모든 검색 필드를 비워둔 경우 전체 데이터를 반환
            if (transactionStartDate == null && transactionEndDate == null &&
                    (materialName == null || materialName.isEmpty()) &&
                    (materialCode == null || materialCode.isEmpty()) &&
                    (companyName == null || companyName.isEmpty()) &&
                    belowSafetyStock == null && stockManagementItem == null) {
                return ResponseEntity.ok(materialTransactionService.getAllTransactions());
            }

            // 검색 필드가 있을 경우 필터링된 데이터를 반환
            List<MaterialTransactionDTO> transactions = materialTransactionService.searchTransactions(
                    transactionStartDate, transactionEndDate, materialName, materialCode, companyName, belowSafetyStock, stockManagementItem);

            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("재료 출납 검색 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 초기화 요청
    @GetMapping("/reset")
    public ResponseEntity<List<MaterialTransactionDTO>> resetTransactions() {
        try {
            // 모든 데이터를 반환
            List<MaterialTransactionDTO> transactions = materialTransactionService.getAllTransactions();
            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception e) {
            log.error("전체 재료 출납 데이터 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


}