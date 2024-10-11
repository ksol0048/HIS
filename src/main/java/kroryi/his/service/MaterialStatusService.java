package kroryi.his.service;

import kroryi.his.dto.MaterialTransactionDTO;
import java.util.List;

public interface MaterialStatusService {
    List<MaterialTransactionDTO> searchMaterials(MaterialTransactionDTO materialTransactionDTO);
}
