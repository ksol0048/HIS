package kroryi.his.service.Impl;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.repository.MaterialStockOutRepository;
import kroryi.his.repository.MaterialTransactionRepository;
import kroryi.his.service.MaterialStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialStatusServiceImpl implements MaterialStatusService {
    private final MaterialTransactionRepository materialTransactionRepository;
    private final MaterialStockOutRepository materialStockOutRepository;
    private final MaterialRegisterRepository materialRegisterRepository;

    // 입고량 또는 출고량 등록 후 재고 현황 업데이트
    @Override
    public void updateMaterialStatus(String materialCode) {
        // 재료 코드로 해당 재료 정보를 가져옴
        MaterialRegister material = materialRegisterRepository.findByMaterialCode(materialCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 재료를 찾을 수 없습니다: " + materialCode));

        // 총 입고량 계산
        Long totalStockIn = materialTransactionRepository.getTotalStockInByMaterialCode(materialCode);
        // 총 출고량 계산
        Long totalStockOut = materialStockOutRepository.getTotalStockOutByMaterialCode(materialCode);

        // null 값 처리
        totalStockIn = (totalStockIn != null) ? totalStockIn : 0L;
        totalStockOut = (totalStockOut != null) ? totalStockOut : 0L;

        // 현재고량 계산 (입고량 - 출고량)
        Long remainingStock = totalStockIn - totalStockOut;

        // 현재고량을 각 입고 및 출고 트랜잭션에 반영
        List<MaterialTransactionRegister> transactions = materialTransactionRepository.findByMaterialRegister(material);
        for (MaterialTransactionRegister transaction : transactions) {
            transaction.setRemainingStock(remainingStock);

            // 최소보관수량과 비교하여 isBelowSafetyStock 설정
            transaction.setBelowSafetyStock(remainingStock < material.getMinQuantity());

            materialTransactionRepository.save(transaction);
        }

        log.info("재료 코드 {}에 대한 현재고량이 각 트랜잭션에 업데이트되었습니다: 남은 재고 {}", materialCode, remainingStock);
    }

    // 전체 트랜잭션에 대해 계산하여 DTO 반환
    @Override
    public List<MaterialTransactionDTO> getAllMaterialTransactions() {
        // 전체 트랜잭션 가져오기
        List<MaterialTransactionRegister> transactions = materialTransactionRepository.findAll();

        // 각 트랜잭션에 대해 남은 재고 계산
        return transactions.stream()
                .map(transaction -> {
                    // 각 트랜잭션의 재료 코드에 대한 총 입고량과 출고량 계산
                    String materialCode = transaction.getMaterialRegister().getMaterialCode();
                    Long totalStockIn = materialTransactionRepository.getTotalStockInByMaterialCode(materialCode);
                    Long totalStockOut = materialStockOutRepository.getTotalStockOutByMaterialCode(materialCode);

                    // null 값 처리
                    totalStockIn = (totalStockIn != null) ? totalStockIn : 0L;
                    totalStockOut = (totalStockOut != null) ? totalStockOut : 0L;

                    // 남은 재고량 계산 (입고량 - 출고량)
                    Long remainingStock = totalStockIn - totalStockOut;


                    // 트랜잭션에 남은 재고 설정
                    transaction.setRemainingStock(remainingStock);

                    // 최소보관수량과 비교하여 isBelowSafetyStock 설정
                    transaction.setBelowSafetyStock(remainingStock < transaction.getMaterialRegister().getMinQuantity());

                    // DTO로 변환 후 반환
                    return new MaterialTransactionDTO(transaction);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialTransactionDTO> searchMaterialStatus(LocalDate transactionStartDate,
                                                             LocalDate transactionEndDate,
                                                             String materialName,
                                                             String materialCode,
                                                             String companyName,
                                                             Boolean belowSafetyStock,
                                                             Boolean stockManagementItem) {

        // 검색 쿼리 실행
        Optional<List<MaterialTransactionRegister>> transactionsOpt = materialTransactionRepository.findSearch(
                transactionStartDate, transactionEndDate, materialName, materialCode, companyName, belowSafetyStock, stockManagementItem);

        // 쿼리 결과가 없을 경우 로그 기록
        if (transactionsOpt.isEmpty()) {
            log.warn("검색 결과가 없습니다.");
            return Collections.emptyList();
        }

        // DTO 변환 과정 로그 기록
        List<MaterialTransactionRegister> transactions = transactionsOpt.get();
        log.info("쿼리 결과 수: {}", transactions.size());

        // DTO로 변환하여 반환
        return transactions.stream()
                .map(MaterialTransactionDTO::new)
                .collect(Collectors.toList());
    }
}




