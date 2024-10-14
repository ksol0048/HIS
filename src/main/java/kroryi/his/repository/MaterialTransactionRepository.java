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

    // MaterialRegister로 검색 (MaterialRegister 자체로 트랜잭션 조회)
    List<MaterialTransactionRegister> findByMaterialRegister(MaterialRegister materialRegister);

    @Query("SELECT mt FROM MaterialTransactionRegister mt " +
            "LEFT JOIN mt.materialRegister mr " +
            "LEFT JOIN mr.companyRegister cr " +  // 업체명 검색을 위해 추가
            "WHERE (:materialName IS NULL OR :materialName = '' OR mr.materialName LIKE %:materialName%) " +
            "AND (:materialCode IS NULL OR :materialCode = '' OR mr.materialCode LIKE %:materialCode%) " +
            "AND (:transactionStartDate IS NULL OR mr.firstRegisterDate >= :transactionStartDate) " +
            "AND (:transactionEndDate IS NULL OR mr.firstRegisterDate <= :transactionEndDate) " +
            "AND (:companyName IS NULL OR :companyName = '' OR cr.companyName LIKE %:companyName%) " +  // 업체명 조건 수정
            "AND (:belowSafetyStock IS NULL OR mt.belowSafetyStock = :belowSafetyStock) " +  // 최소 보관수량 미달 조건
            "AND (:stockManagementItem IS NULL OR mr.stockManagementItem = :stockManagementItem)")  // 재고 관리 품목 조건 추가
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


}
