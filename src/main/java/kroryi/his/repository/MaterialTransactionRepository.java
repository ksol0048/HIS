package kroryi.his.repository;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.io.*;

public interface MaterialTransactionRepository extends JpaRepository<MaterialTransactionRegister, Long> {
    // 재료명으로 검색
    List<MaterialTransactionRegister> findByMaterialRegisterMaterialNameContaining(String materialName);

    // 재료코드로 검색
    List<MaterialTransactionRegister> findByMaterialRegisterMaterialCodeContaining(String materialCode);

    Optional<MaterialTransactionRegister> findBystockInDateAndMaterialRegister(LocalDate stockInDate, MaterialRegister materialRegister);

    @Query("SELECT mt FROM MaterialTransactionRegister mt " +
            "JOIN mt.materialRegister mr " +
            "WHERE (mr.materialName LIKE %:materialName% OR mr.materialCode LIKE %:materialCode%) " +
            "OR mt.stockInDate BETWEEN :startDate AND :endDate")
    Optional<List<MaterialTransactionRegister>> findSearch(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("materialName") String materialName,
            @Param("materialCode") String materialCode);
}
