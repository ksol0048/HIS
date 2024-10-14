package kroryi.his.service;

import kroryi.his.dto.MaterialTransactionDTO;

import java.time.LocalDate;
import java.util.List;

public interface MaterialStatusService {

    // 입고량 또는 출고량 등록 후 재고 현황 업데이트
    void updateMaterialStatus(String materialCode);

    // 재료 현황 검색
    List<MaterialTransactionDTO> searchMaterialStatus(LocalDate transactionStartDate,
                                                      LocalDate transactionEndDate,
                                                      String materialName,
                                                      String materialCode,
                                                      String companyName,
                                                      Boolean belowSafetyStock,
                                                      Boolean stockManagementItem) ;

    // 전체 데이터를 반환하는 메서드 추가
    List<MaterialTransactionDTO> getAllMaterialTransactions();
}
