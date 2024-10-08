package kroryi.his.service;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;
import kroryi.his.dto.MaterialTransactionDTO;

import java.time.LocalDate;
import java.util.List;

public interface MaterialTransactionService {
    MaterialTransactionRegister register(MaterialTransactionDTO materialTransactionDTO);

    MaterialTransactionRegister update(MaterialTransactionDTO materialTransactionDTO);

    List<MaterialTransactionDTO> getAllTransactions();

    List<MaterialTransactionDTO> searchByMaterialName(String materialName);

    List<MaterialTransactionDTO> searchByMaterialCode(String materialCode);

    List<MaterialTransactionDTO> searchTransactions(LocalDate transactionStartDate, LocalDate transactionEndDate, String materialName, String materialCode);

    void deleteByTransactionId(Long transactionId);
}
