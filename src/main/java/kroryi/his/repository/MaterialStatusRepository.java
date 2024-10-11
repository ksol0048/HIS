package kroryi.his.repository;

import kroryi.his.domain.MaterialTransactionRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaterialStatusRepository extends JpaRepository<MaterialTransactionRegister, Long> {

    @Query("SELECT m FROM MaterialTransactionRegister m " +
            "WHERE (:companyName IS NULL OR m.materialRegister.companyRegister.companyName LIKE %:companyName%) " +
            "AND (:materialName IS NULL OR m.materialRegister.materialName LIKE %:materialName%) " +
            "AND (:referenceDate IS NULL OR m.stockInDate = :referenceDate) " +
            "AND (:belowSafetyStock IS NULL OR m.belowSafetyStock = :belowSafetyStock) " +
            "AND (:stockManagementItem IS NULL OR m.materialRegister.stockManagementItem = :stockManagementItem)")
    List<MaterialTransactionRegister> findBySearchParams(
            @Param("companyName") String companyName,
            @Param("materialName") String materialName,
            @Param("referenceDate") LocalDate referenceDate,
            @Param("belowSafetyStock") Boolean belowSafetyStock,
            @Param("stockManagementItem") Boolean stockManagementItem
    );
}
