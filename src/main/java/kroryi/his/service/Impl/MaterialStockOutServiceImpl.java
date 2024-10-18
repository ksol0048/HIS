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
    public void updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        if (stockOutDTO.getStockOutId() == null) {
            throw new IllegalArgumentException("수정하려면 출고 ID가 필요합니다.");
        }

        MaterialStockOut existingStockOut = materialStockOutRepository.findById(stockOutDTO.getStockOutId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 출고 데이터를 찾을 수 없습니다. ID: " + stockOutDTO.getStockOutId()));

        // 기존 데이터와 비교하여 수정된 내용이 있는지 확인
        boolean isModified = false;

        if (!existingStockOut.getStockOutDate().equals(stockOutDTO.getStockOutDate())) {
            existingStockOut.setStockOutDate(stockOutDTO.getStockOutDate());
            isModified = true;
        }

        if (!existingStockOut.getStockOut().equals(stockOutDTO.getStockOut())) {
            // 재고량을 초과하는지 확인
            MaterialRegister materialRegister = existingStockOut.getMaterialRegister();
            Long remainingStock = calculateRemainingStock(materialRegister);
            Long adjustedRemainingStock = remainingStock + existingStockOut.getStockOut();

            if (stockOutDTO.getStockOut() > adjustedRemainingStock) {
                throw new IllegalArgumentException("수정된 출고량이 현재 재고량보다 많습니다.");
            }

            existingStockOut.setStockOut(stockOutDTO.getStockOut());
            isModified = true;
        }

        // 수정된 부분이 있으면 저장
        if (isModified) {
            materialStockOutRepository.save(existingStockOut);
        } else {
            log.info("수정된 내용이 없어 저장하지 않았습니다.");
        }
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