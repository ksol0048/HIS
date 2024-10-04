package kroryi.his.controller;

import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialTransactionController {

    @Autowired
    private MaterialTransactionService materialTransactionService;

    @GetMapping("/searchTransaction")
    public ResponseEntity<List<MaterialTransactionDTO>> searchTransaction(
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String materialCode) {
        List<MaterialTransactionDTO> transactions = materialTransactionService.searchTransactions(materialName, materialCode);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/addTransaction")
    public ResponseEntity<Map<String, String>> addTransaction(@RequestBody MaterialTransactionDTO transactionDTO) {
        materialTransactionService.addTransaction(transactionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "출납 내역이 추가되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateTransaction")
    public ResponseEntity<Map<String, String>> updateTransaction(@RequestBody MaterialTransactionDTO transactionDTO) {
        materialTransactionService.updateTransaction(transactionDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "출납 내역이 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deleteTransaction")
    public ResponseEntity<Map<String, String>> deleteTransaction(@RequestParam Long transactionId) {
        materialTransactionService.deleteTransaction(transactionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "출납 내역이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }


}