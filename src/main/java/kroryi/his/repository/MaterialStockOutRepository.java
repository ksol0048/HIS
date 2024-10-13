package kroryi.his.repository;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialStockOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialStockOutRepository extends JpaRepository<MaterialStockOut, Long> {

    List<MaterialStockOut> findByMaterialRegister_MaterialCode(String materialCode);

}
