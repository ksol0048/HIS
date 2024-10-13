package kroryi.his.service;

import kroryi.his.dto.MaterialStockOutDTO;

import java.util.List;

public interface MaterialStockOutService {
    void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO);

    List<MaterialStockOutDTO> getOutgoingTransactionsByMaterialCode(String materialCode);

}
