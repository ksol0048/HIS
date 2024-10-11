package kroryi.his.controller;

import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.service.MaterialStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory_management")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@Log4j2
@RequiredArgsConstructor
public class MaterialStatusController {

    private final MaterialStatusService materialStatusService;

    // 검색 조건에 따라 데이터를 가져오는 엔드포인트
    @PostMapping("/statusSearch")
    public List<MaterialTransactionDTO> searchMaterials(@RequestBody MaterialTransactionDTO materialTransactionDTO) {
        log.info("검색 조건: {}", materialTransactionDTO);
        List<MaterialTransactionDTO> result = materialStatusService.searchMaterials(materialTransactionDTO);
        log.info("검색 결과: {}", result);
        return result;
    }

}
