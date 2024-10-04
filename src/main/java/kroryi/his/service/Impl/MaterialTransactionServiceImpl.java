package kroryi.his.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.repository.CompanyRegisterRepository;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.repository.MaterialTransactionRepository;
import kroryi.his.service.MaterialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialTransactionServiceImpl implements MaterialTransactionService {

    private final MaterialRegisterRepository materialRepository;
    private final CompanyRegisterRepository companyRepository;
    private final MaterialTransactionRepository materialTransactionRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<MaterialTransactionDTO> searchTransactions(String materialName, String materialCode) {
        if (materialName != null || materialCode != null) {
            return materialTransactionRepository.findByMaterialRegister_MaterialNameOrMaterialRegister_MaterialCode(materialName, materialCode)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            return materialTransactionRepository.findAll()
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void addTransaction(MaterialTransactionDTO transactionDTO) {
        MaterialTransactionRegister transaction = convertToEntity(transactionDTO);
        materialTransactionRepository.save(transaction);
    }

    @Override
    public void updateTransaction(MaterialTransactionDTO transactionDTO) {
        MaterialTransactionRegister transaction = materialTransactionRepository.findById(transactionDTO.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setStockIn(transactionDTO.getStockIn());
        transaction.setStockOut(transactionDTO.getStockOut());
        transaction.setRemainingStock(transactionDTO.getRemainingStock());
        transaction.setBelowSafetyStock(transactionDTO.isBelowSafetyStock());

        materialTransactionRepository.save(transaction);
    }

    @Override
    public void deleteTransaction(Long transactionId) {
        MaterialTransactionRegister transaction = materialTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        materialTransactionRepository.delete(transaction);
    }

    private MaterialTransactionDTO convertToDTO(MaterialTransactionRegister transaction) {
        return new MaterialTransactionDTO(transaction);
    }

    private MaterialTransactionRegister convertToEntity(MaterialTransactionDTO transactionDTO) {
        // Optional에서 값을 꺼내는 부분 수정
        MaterialRegister materialRegister = materialRepository.findByMaterialCode(transactionDTO.getMaterialCode())
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        return MaterialTransactionRegister.builder()
                .transactionDate(transactionDTO.getTransactionDate())
                .stockIn(transactionDTO.getStockIn())
                .stockOut(transactionDTO.getStockOut())
                .remainingStock(transactionDTO.getRemainingStock())
                .belowSafetyStock(transactionDTO.isBelowSafetyStock())
                .materialRegister(materialRegister)  // Optional 타입이 아닌 실제 객체를 전달
                .build();
    }


}
