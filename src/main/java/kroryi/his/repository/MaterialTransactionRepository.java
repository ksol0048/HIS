package kroryi.his.repository;

import kroryi.his.domain.MaterialTransactionRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialTransactionRepository extends JpaRepository<MaterialTransactionRegister, Long> {
    List<MaterialTransactionRegister> findByMaterialRegister_MaterialNameOrMaterialRegister_MaterialCode(String materialName, String materialCode);
}
