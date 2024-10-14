package kroryi.his.service.Impl;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialStockOut;
import kroryi.his.dto.MaterialStockOutDTO;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.repository.MaterialStockOutRepository;
import kroryi.his.service.MaterialStockOutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialStockOutServiceImpl implements MaterialStockOutService {

    private final MaterialStockOutRepository materialStockOutRepository;
    private final MaterialRegisterRepository materialRegisterRepository;

    // 신규 출고 데이터 저장
    @Override
    public void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        MaterialStockOut stockOut = new MaterialStockOut();
        stockOut.setStockOutDate(stockOutDTO.getStockOutDate());
        stockOut.setStockOut(stockOutDTO.getStockOut());
        stockOut.setMaterialRegister(materialRegisterRepository.findByMaterialCode(stockOutDTO.getMaterialCode())
                .orElseThrow(() -> new IllegalArgumentException("재료 코드를 찾을 수 없습니다.")));

        materialStockOutRepository.save(stockOut);
    }

    // 기존 출고 데이터 수정
    @Override
    public void updateOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        if (stockOutDTO.getStockOutId() == null) {
            throw new IllegalArgumentException("수정하려면 출고 ID가 필요합니다.");
        }

        MaterialStockOut stockOut = materialStockOutRepository.findById(stockOutDTO.getStockOutId())
                .orElseThrow(() -> new IllegalArgumentException("해당 출고 데이터를 찾을 수 없습니다."));

        stockOut.setStockOutDate(stockOutDTO.getStockOutDate());
        stockOut.setStockOut(stockOutDTO.getStockOut());

        materialStockOutRepository.save(stockOut);
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
