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

    @Override
    public void saveOutgoingTransaction(MaterialStockOutDTO stockOutDTO) {
        // materialCode로 MaterialRegister 찾기
        Optional<MaterialRegister> materialOpt = materialRegisterRepository.findByMaterialCode(stockOutDTO.getMaterialCode());

        if (materialOpt.isPresent()) {
            MaterialStockOut stockOut = new MaterialStockOut();
            stockOut.setMaterialRegister(materialOpt.get());  // MaterialRegister 설정
            stockOut.setStockOutDate(stockOutDTO.getStockOutDate());  // 출고일자 설정
            stockOut.setStockOut(stockOutDTO.getStockOut());  // 출고량 설정

            materialStockOutRepository.save(stockOut);  // 저장
        } else {
            throw new IllegalArgumentException("해당 재료 코드를 찾을 수 없습니다: " + stockOutDTO.getMaterialCode());
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
}
