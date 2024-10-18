package kroryi.his.service;

import kroryi.his.dto.MaterialStockOutDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MaterialStockOutService {
    void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO);

    List<MaterialStockOutDTO> getOutgoingTransactionsByMaterialCode(String materialCode);

    void deleteByTransactionId(Long transactionId);

    Map<String, Object> updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO);
}