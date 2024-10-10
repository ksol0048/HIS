package kroryi.his.repository;

import kroryi.his.domain.CompanyRegister;
import kroryi.his.domain.MaterialRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MaterialRegisterRepository extends JpaRepository<MaterialRegister, String> {

    boolean existsByMaterialCode(String materialCode);

    @Modifying
    @Transactional
    @Query("DELETE FROM MaterialRegister m WHERE m.materialCode = :materialCode")
    void customDeleteByMaterialCode(@Param("materialCode") String materialCode);

    // 업체명으로 검색
    List<MaterialRegister> findByCompanyRegisterCompanyNameContaining(String companyName);

    // 재료명으로 검색
    List<MaterialRegister> findByMaterialNameContaining(String materialName);

    // 업체명과 재료명으로 검색
    List<MaterialRegister> findByCompanyRegisterCompanyNameContainingAndMaterialNameContaining(String companyName, String materialName);

    Optional<MaterialRegister> findByMaterialCode(String materialCode);

}
