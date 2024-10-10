package kroryi.his.repository;

import kroryi.his.domain.MaterialTransactionRegister;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialStatusRepository extends JpaRepository<MaterialTransactionRegister, Long> {
}
