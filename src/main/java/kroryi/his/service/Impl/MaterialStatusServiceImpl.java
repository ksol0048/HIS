package kroryi.his.service.Impl;

import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.repository.MaterialStatusRepository;
import kroryi.his.service.MaterialStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaterialStatusServiceImpl implements MaterialStatusService {

    private final MaterialStatusRepository materialStatusRepository;

    @Autowired
    public MaterialStatusServiceImpl(MaterialStatusRepository materialStatusRepository) {
        this.materialStatusRepository = materialStatusRepository;
    }

    @Override
    public List<MaterialTransactionDTO> searchMaterials(MaterialTransactionDTO searchParams) {
        List<MaterialTransactionRegister> transactions = materialStatusRepository.findBySearchParams(
                searchParams.getCompanyName(),
                searchParams.getMaterialName(),
                searchParams.getFirstRegisterDate(),
                searchParams.isBelowSafetyStock(),
                searchParams.getStockManagementItem()
        );

        // 재료 코드와 회사명별로 데이터를 그룹화하여 합산합니다.
        Map<String, MaterialTransactionDTO> groupedByMaterialCodeAndCompanyName = new HashMap<>();

        for (MaterialTransactionRegister transaction : transactions) {
            String materialCode = transaction.getMaterialRegister().getMaterialCode();
            String companyName = transaction.getMaterialRegister().getCompanyRegister().getCompanyName();
            String key = materialCode + "_" + companyName;

            Long stockIn = transaction.getStockIn() != null ? transaction.getStockIn() : 0L;
            Long stockOut = transaction.getStockOut() != null ? transaction.getStockOut() : 0L;

            groupedByMaterialCodeAndCompanyName.compute(key, (k, existingDto) -> {
                if (existingDto == null) {
                    MaterialTransactionDTO dto = new MaterialTransactionDTO(transaction);
                    dto.setStockIn(stockIn);
                    dto.setStockOut(stockOut);
                    dto.setRemainingStock(stockIn - stockOut);
                    return dto;
                } else {
                    existingDto.setStockIn(existingDto.getStockIn() + stockIn);
                    existingDto.setStockOut(existingDto.getStockOut() + stockOut);
                    existingDto.setRemainingStock(existingDto.getStockIn() - existingDto.getStockOut());
                    return existingDto;
                }
            });
        }

        // DTO 목록으로 변환하여 반환합니다.
        return new ArrayList<>(groupedByMaterialCodeAndCompanyName.values());
    }
}
