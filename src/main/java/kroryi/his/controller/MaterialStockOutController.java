package kroryi.his.controller;

import kroryi.his.dto.MaterialStockOutDTO;
import kroryi.his.service.MaterialStockOutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialStockOutController {

    private final MaterialStockOutService materialStockOutService;


    // 신규 출고 데이터 추가
    @PostMapping("/addStockTransaction")
    public ResponseEntity<?> saveStockOut(@RequestBody MaterialStockOutDTO stockOutDTO) {
        try {
            materialStockOutService.saveOutgoingTransaction(stockOutDTO);
            return ResponseEntity.ok(Map.of("success", true, "message", "출고 내역이 저장되었습니다."));
        } catch (IllegalArgumentException e) {
            // 재고 부족으로 인한 예외 처리
            return ResponseEntity.ok(Map.of("success", false, "message", "현재고량을 초과해 저장할 수 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "서버 오류가 발생했습니다."));
        }
    }


    // 기존 출고 데이터 수정
    @PutMapping("/updateStockTransaction")
    public ResponseEntity<?> updateStockTransaction(@RequestBody MaterialStockOutDTO stockOutDTO) {
        try {
            materialStockOutService.updateOutgoingTransaction(stockOutDTO);
            log.info("Stock transaction updated: {}", stockOutDTO);
            return ResponseEntity.ok(Map.of("success", true, "message", "출고 데이터가 수정되었습니다."));
        } catch (Exception e) {
            log.error("Error updating stock transaction", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }


    @GetMapping("/getByMaterialCode")
    public ResponseEntity<List<MaterialStockOutDTO>> getOutgoingTransactions(@RequestParam String materialCode) {
        try {
            List<MaterialStockOutDTO> stockOutList = materialStockOutService.getOutgoingTransactionsByMaterialCode(materialCode);
            return ResponseEntity.ok(stockOutList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // 출납 기록 삭제 (DELETE 요청 처리)
    @DeleteMapping("/deleteStockOut/{stockOutId}")
    public ResponseEntity<?> deleteStockOutTransaction(@PathVariable("stockOutId") Long stockOutId) {
        try {
            materialStockOutService.deleteByTransactionId(stockOutId);  // 서비스에 삭제 요청
            log.info("StockOut Deleted for StockOut ID: {}", stockOutId);
            return ResponseEntity.ok(Map.of("success", true, "message", "출고 기록이 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.error("출고 기록 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("출고 기록 삭제 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}