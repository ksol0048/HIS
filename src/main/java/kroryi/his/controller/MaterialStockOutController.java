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

    @PostMapping("/add")
    public ResponseEntity<?> saveStockOut(@RequestBody MaterialStockOutDTO stockOutDTO) {
        try {
            materialStockOutService.saveOutgoingTransaction(stockOutDTO);
            return ResponseEntity.ok(Map.of("success", true, "message", "출고 내역이 저장되었습니다."));
        } catch (Exception e) {
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
}
