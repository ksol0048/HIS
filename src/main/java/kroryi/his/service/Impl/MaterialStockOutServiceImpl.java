package kroryi.his.service.Impl;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialStockOut;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialStockOutDTO;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.repository.MaterialStockOutRepository;
import kroryi.his.service.MaterialStockOutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialStockOutServiceImpl implements MaterialStockOutService {

    private final MaterialStockOutRepository materialStockOutRepository;
    private final MaterialRegisterRepository materialRegisterRepository;

    @Override
    public ResponseEntity<?> saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        try {
            MaterialRegister materialRegister = materialRegisterRepository.findByMaterialCode(stockOutDTO.getMaterialCode())
                    .orElseThrow(() -> new IllegalArgumentException("재료 코드를 찾을 수 없습니다."));

            Long remainingStock = calculateRemainingStock(materialRegister);

            // 출고량이 현재 재고량보다 많은 경우 예외 처리
            if (stockOutDTO.getStockOut() > remainingStock) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "현재고량을 초과해 저장할 수 없습니다."));
            }

            // 새로운 출고 데이터를 저장
            MaterialStockOut stockOut = new MaterialStockOut();
            stockOut.setStockOutDate(stockOutDTO.getStockOutDate());
            stockOut.setStockOut(stockOutDTO.getStockOut());
            stockOut.setMaterialRegister(materialRegister);

            materialStockOutRepository.save(stockOut);
            return ResponseEntity.ok(Map.of("success", true, "message", "출고가 성공적으로 저장되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "출고 저장 중 오류가 발생했습니다."));
        }
    }

    // 현재 재고량을 계산하는 메서드
    private Long calculateRemainingStock(MaterialRegister materialRegister) {
        List<MaterialTransactionRegister> transactions = materialRegister.getMaterialTransactionList();

        // 총 입고량 계산
        Long totalStockIn = transactions.stream()
                .mapToLong(MaterialTransactionRegister::getStockIn)
                .sum();

        // 총 출고량 계산
        Long totalStockOut = materialRegister.getMaterialStockOutList().stream()
                .mapToLong(MaterialStockOut::getStockOut)
                .sum();

        return totalStockIn - totalStockOut;
    }

    // 기존 출고 데이터를 수정하는 메서드
    @Override
    public void updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        if (stockOutDTO.getStockOutId() == null) {
            throw new IllegalArgumentException("수정하려면 출고 ID가 필요합니다.");
        }

        MaterialStockOut existingStockOut = materialStockOutRepository.findById(stockOutDTO.getStockOutId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 출고 데이터를 찾을 수 없습니다. ID: " + stockOutDTO.getStockOutId()));

        // 재료 정보를 MaterialRegister에서 가져옴
        MaterialRegister materialRegister = existingStockOut.getMaterialRegister();

        // 현재 재고량을 계산
        Long remainingStock = calculateRemainingStock(materialRegister);

        // 기존 출고량을 더한 후에 남은 재고량과 수정하려는 출고량을 비교
        Long adjustedRemainingStock = remainingStock + existingStockOut.getStockOut();

        if (stockOutDTO.getStockOut() > adjustedRemainingStock) {
            throw new IllegalArgumentException("수정된 출고량이 현재 재고량보다 많습니다.");
        }

        // 기존 출고 데이터를 수정
        existingStockOut.setStockOutDate(stockOutDTO.getStockOutDate());
        existingStockOut.setStockOut(stockOutDTO.getStockOut());
        materialStockOutRepository.save(existingStockOut);
    }

    @Override
    public List<MaterialStockOutDTO> getOutgoingTransactionsByMaterialCode(String materialCode) {
        // materialCode로 출고 내역을 정확히 필터링
        return materialStockOutRepository.findByMaterialRegister_MaterialCode(materialCode)
                .stream()
                .map(stockOut -> new MaterialStockOutDTO(
                        stockOut.getStockOutId(),
                        stockOut.getStockOutDate(),
                        stockOut.getStockOut(),
                        stockOut.getMaterialRegister().getMaterialCode(),
                        stockOut.getMaterialRegister().getMaterialName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTransactionId(Long stockOutId) {
        Optional<MaterialStockOut> transaction = materialStockOutRepository.findById(stockOutId);

        if (transaction.isPresent()) {
            materialStockOutRepository.deleteById(stockOutId);  // 삭제 처리
            log.info("StockOut ID {} 삭제 성공", stockOutId);
        } else {
            throw new IllegalArgumentException("해당 ID의 출납 기록이 존재하지 않습니다: " + stockOutId);
        }
    }
}
