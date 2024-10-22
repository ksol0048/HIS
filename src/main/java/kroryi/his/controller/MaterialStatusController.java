package kroryi.his.controller;

import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialStatusController {
    private final MaterialStatusService materialStatusService;

    @GetMapping("/materialStatus/search")
    public ResponseEntity<List<MaterialTransactionDTO>> searchMaterialStatus(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String materialCode,
            @RequestParam(required = false) Boolean belowSafetyStock,
            @RequestParam(required = false) Boolean stockManagementItem) {
        try {
            LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : null;
            LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : null;

            List<MaterialTransactionDTO> result = materialStatusService.searchMaterialStatus(
                    start, end, materialName, materialCode, companyName, belowSafetyStock, stockManagementItem);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("서버 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }



}
