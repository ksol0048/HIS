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

import java.util.HashMap;
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
    public void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        MaterialRegister materialRegister = materialRegisterRepository.findByMaterialCode(stockOutDTO.getMaterialCode())
                .orElseThrow(() -> new IllegalArgumentException("재료 코드를 찾을 수 없습니다."));

        // 현재 재고량을 계산
        Long remainingStock = calculateRemainingStock(materialRegister);

        // 출고량이 현재 재고량보다 많은 경우 예외 처리
        if (stockOutDTO.getStockOut() > remainingStock) {
            throw new IllegalArgumentException("출고량이 현재 재고량보다 많습니다.");
        }

        // 새로운 출고 데이터를 저장
        MaterialStockOut stockOut = new MaterialStockOut();
        stockOut.setStockOutDate(stockOutDTO.getStockOutDate());
        stockOut.setStockOut(stockOutDTO.getStockOut());
        stockOut.setMaterialRegister(materialRegister);

        materialStockOutRepository.save(stockOut);
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

    @Override
    public Map<String, Object> updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        // 출고 ID가 없는 경우 예외 처리
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

        // 수정된 출고량이 재고량을 초과하는지 확인
        if (stockOutDTO.getStockOut() > adjustedRemainingStock) {
            // 재고량 초과 시 메시지 반환
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "수정된 출고량이 현재 재고량보다 많습니다.");
            return response;
        }

        // 기존 출고 데이터를 수정
        existingStockOut.setStockOutDate(stockOutDTO.getStockOutDate());
        existingStockOut.setStockOut(stockOutDTO.getStockOut());
        materialStockOutRepository.save(existingStockOut);

        // 성공 메시지 반환
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "출고 내역이 성공적으로 수정되었습니다.");
        return response;
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