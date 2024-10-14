package kroryi.his.repository;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialStockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaterialStockOutRepository extends JpaRepository<MaterialStockOut, Long> {

    List<MaterialStockOut> findByMaterialRegister_MaterialCode(String materialCode);

    // materialCode별 총 출고량 계산
    @Query("SELECT SUM(mso.stockOut) FROM MaterialStockOut mso WHERE mso.materialRegister.materialCode = :materialCode")
    Long getTotalStockOutByMaterialCode(@Param("materialCode") String materialCode);

}
