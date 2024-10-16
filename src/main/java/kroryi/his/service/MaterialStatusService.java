package kroryi.his.service;

import kroryi.his.dto.MaterialTransactionDTO;

import java.time.LocalDate;
import java.util.List;

public interface MaterialStatusService {


    // 재료 현황 검색
    List<MaterialTransactionDTO> searchMaterialStatus(LocalDate transactionStartDate,
                                                      LocalDate transactionEndDate,
                                                      String materialName,
                                                      String materialCode,
                                                      String companyName,
                                                      Boolean belowSafetyStock,
                                                      Boolean stockManagementItem) ;


}
