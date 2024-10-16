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

        if (transactionsOpt.isEmpty()) {
            log.warn("검색 결과가 없습니다.");
            return Collections.emptyList();
        }

        List<MaterialTransactionRegister> transactions = transactionsOpt.get();
        log.info("쿼리 결과 수: {}", transactions.size());

        // materialCode를 기준으로 그룹화
        return transactions.stream()
                .collect(Collectors.groupingBy(transaction -> transaction.getMaterialRegister().getMaterialCode()))
                .values().stream() // 그룹화된 결과를 스트림으로 변환
                .map(groupedTransactions -> {
                    MaterialTransactionRegister firstTransaction = groupedTransactions.get(0); // 그룹에서 첫 번째 트랜잭션 불러오기
                    MaterialTransactionDTO dto = new MaterialTransactionDTO(firstTransaction);

                    String materialCodeFromTransaction = firstTransaction.getMaterialRegister().getMaterialCode();
                    Long totalStockIn = materialTransactionRepository.getTotalStockInByMaterialCode(materialCodeFromTransaction);
                    Long totalStockOut = materialStockOutRepository.getTotalStockOutByMaterialCode(materialCodeFromTransaction);

                    totalStockIn = (totalStockIn != null) ? totalStockIn : 0L;
                    totalStockOut = (totalStockOut != null) ? totalStockOut : 0L;

                    Long remainingStock = totalStockIn - totalStockOut;

                    boolean isBelowSafetyStock = remainingStock < firstTransaction.getMaterialRegister().getMinQuantity();
                    dto.setRemainingStock(remainingStock);
                    dto.setBelowSafetyStock(isBelowSafetyStock);

                    // DB에 실시간으로 belowSafetyStock 값 업데이트
                    materialTransactionRepository.updateBelowSafetyStock(firstTransaction.getTransactionId(), isBelowSafetyStock);

                    // 하이라이트 설정
                    dto.setHighlighted(isBelowSafetyStock && (dto.getStockManagementItem() != null && dto.getStockManagementItem()));


                    return dto;
                })
                .collect(Collectors.toList());
    }
}


