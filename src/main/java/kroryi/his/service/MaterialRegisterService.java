package kroryi.his.service;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;

import java.util.List;

public interface MaterialRegisterService {
    List<MaterialRegister> getAllMaterial();

    List<MaterialDTO> getAllMaterialsWithCompany();

    MaterialRegister register(MaterialDTO materialDTO);

    void customDeleteByMaterialCode(String materialCode);

    List<MaterialRegister> searchByCompanyName(String companyName);

    List<MaterialRegister> searchByMaterialName(String materialName);

    List<MaterialRegister> searchByCompanyNameAndMaterialName(String companyName , String materialName);

    void updateMaterial(MaterialDTO materialDTO);

    boolean isNewMaterial(String materialCode);

}
