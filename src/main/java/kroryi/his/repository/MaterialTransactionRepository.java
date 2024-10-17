package kroryi.his.repository;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    // MaterialRegister로 검색 (MaterialRegister 자체로 트랜잭션 조회)
    List<MaterialTransactionRegister> findByMaterialRegister(MaterialRegister materialRegister);

    @Query("SELECT mt FROM MaterialTransactionRegister mt " +
            "JOIN mt.materialRegister mr " +
            "JOIN mr.companyRegister cr " +
            "WHERE (:materialName IS NULL OR LOWER(mr.materialName) LIKE LOWER(:materialName)) " +
            "AND (:materialCode IS NULL OR LOWER(mr.materialCode) LIKE LOWER(:materialCode)) " +
            "AND (:companyName IS NULL OR LOWER(cr.companyName) LIKE LOWER(:companyName)) " +
            "AND (:transactionStartDate IS NULL OR mt.stockInDate >= :transactionStartDate) " +
            "AND (:transactionEndDate IS NULL OR mt.stockInDate <= :transactionEndDate) " +
            "AND (:belowSafetyStock IS NULL OR mt.belowSafetyStock = :belowSafetyStock) " +
            "AND (:stockManagementItem IS NULL OR mr.stockManagementItem = :stockManagementItem)")
    Optional<List<MaterialTransactionRegister>> findSearch(
            @Param("transactionStartDate") LocalDate transactionStartDate,
            @Param("transactionEndDate") LocalDate transactionEndDate,
            @Param("materialName") String materialName,
            @Param("materialCode") String materialCode,
            @Param("companyName") String companyName,
            @Param("belowSafetyStock") Boolean belowSafetyStock,
            @Param("stockManagementItem") Boolean stockManagementItem);


    // materialCode별 총 입고량 계산
    @Query("SELECT SUM(mtr.stockIn) FROM MaterialTransactionRegister mtr WHERE mtr.materialRegister.materialCode = :materialCode")
    Long getTotalStockInByMaterialCode(@Param("materialCode") String materialCode);

    // belowSafetyStock 실시간 데이터 반영
    @Modifying
    @Transactional
    @Query("UPDATE MaterialTransactionRegister mtr SET mtr.belowSafetyStock = :belowSafetyStock WHERE mtr.transactionId = :transactionId")
    void updateBelowSafetyStock(@Param("transactionId") Long transactionId, @Param("belowSafetyStock") Boolean belowSafetyStock);


}
