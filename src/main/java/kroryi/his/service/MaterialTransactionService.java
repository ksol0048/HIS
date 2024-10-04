package kroryi.his.service;

import kroryi.his.dto.MaterialTransactionDTO;

import java.util.List;

public interface MaterialTransactionService {
    List<MaterialTransactionDTO> searchTransactions(String materialName, String materialCode);
    void addTransaction(MaterialTransactionDTO transactionDTO);
    void updateTransaction(MaterialTransactionDTO transactionDTO);
    void deleteTransaction(Long transactionId);
}
