package kroryi.his.controller;

import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
@Log4j2
@RequiredArgsConstructor
public class HomeFunctionController {
    private final MaterialStatusService materialStatusService;

    @GetMapping("/low-stock-items")
    public List<MaterialTransactionDTO> getLowStockItems() {
        return materialStatusService.getLowStockItems(); // 저조한 재고 항목 반환
    }
}
