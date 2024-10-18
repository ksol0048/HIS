package kroryi.his.service;

import kroryi.his.dto.MaterialStockOutDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface MaterialStockOutService {
    void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO);

    List<MaterialStockOutDTO> getOutgoingTransactionsByMaterialCode(String materialCode);

    void deleteByTransactionId(Long transactionId);

    void updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO);
}