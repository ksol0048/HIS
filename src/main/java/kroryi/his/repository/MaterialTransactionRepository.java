package kroryi.his.repository;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialTransactionRepository extends JpaRepository<MaterialTransactionRegister, Long> {
    List<MaterialTransactionRegister> findByMaterialRegister_MaterialNameOrMaterialRegister_MaterialCode(String materialName, String materialCode);

    // 재료명으로 검색
    List<MaterialTransactionRegister> findByMaterialRegisterMaterialNameContaining(String materialName);

    // 재료코드로 검색
    List<MaterialTransactionRegister> findByMaterialRegisterMaterialCodeContaining(String materialCode);

    // 재료명과 재료코드로 검색
    List<MaterialTransactionRegister> findByMaterialRegisterMaterialNameContainingAndMaterialRegisterMaterialCodeContaining(String materialName, String materialCode);

    Optional<MaterialTransactionRegister> findByMaterialRegisterMaterialCode(String materialCode);
}
