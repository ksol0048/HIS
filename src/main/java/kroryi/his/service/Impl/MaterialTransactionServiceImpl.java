package kroryi.his.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialTransactionDTO;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.repository.MaterialTransactionRepository;
import kroryi.his.service.MaterialTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialTransactionServiceImpl implements MaterialTransactionService {

    @PersistenceContext
    private EntityManager entityManager;

    private final MaterialRegisterRepository materialRepository;

    private final MaterialTransactionRepository materialTransactionRepository;

    private final ModelMapper modelMapper;


    @Override
    public MaterialTransactionRegister register(MaterialTransactionDTO materialTransactionDTO) {
        Optional<MaterialRegister> materialRegisterOpt = materialRepository.findByMaterialCode(materialTransactionDTO.getMaterialCode());

        if (materialRegisterOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 재료 코드를 찾을 수 없습니다: " + materialTransactionDTO.getMaterialCode());
        }

        // 중복 출납 데이터 존재 여부 확인
        Optional<MaterialTransactionRegister> existingTransaction = materialTransactionRepository
                .findBystockInDateAndMaterialRegister(materialTransactionDTO.getStockInDate(), materialRegisterOpt.get());

        if (existingTransaction.isPresent()) {
            throw new IllegalArgumentException("해당 날짜에 이미 출납 기록이 존재합니다.");
        }

        MaterialTransactionRegister transaction = new MaterialTransactionRegister();
        transaction.setStockInDate(materialTransactionDTO.getStockInDate());
        transaction.setMaterialRegister(materialRegisterOpt.get());
        transaction.setStockIn(materialTransactionDTO.getStockIn());
//        transaction.setStockOut(materialTransactionDTO.getStockOut());

        return materialTransactionRepository.save(transaction);
    }


    @Override
    public MaterialTransactionRegister update(MaterialTransactionDTO materialTransactionDTO) {
        if (materialTransactionDTO.getTransactionId() == null) {
            throw new IllegalArgumentException("출납 정보 ID가 누락되었습니다.");
        }

        Optional<MaterialTransactionRegister> transactionOpt = materialTransactionRepository.findById(materialTransactionDTO.getTransactionId());

        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 출납 정보를 찾을 수 없습니다: " + materialTransactionDTO.getTransactionId());
        }

        MaterialTransactionRegister transaction = transactionOpt.get();
        transaction.setStockInDate(materialTransactionDTO.getStockInDate());
        transaction.setStockIn(materialTransactionDTO.getStockIn());
//        transaction.setStockOut(materialTransactionDTO.getStockOut());

        // 추가적으로 materialCode나 다른 정보가 변경되었을 경우를 처리
        Optional<MaterialRegister> materialRegisterOpt = materialRepository.findByMaterialCode(materialTransactionDTO.getMaterialCode());
        if (materialRegisterOpt.isEmpty()) {
            throw new IllegalArgumentException("해당 재료 코드를 찾을 수 없습니다: " + materialTransactionDTO.getMaterialCode());
        }
        transaction.setMaterialRegister(materialRegisterOpt.get());

        return materialTransactionRepository.save(transaction);
    }

    // transactionId로 삭제
    public void deleteByTransactionId(Long transactionId) {
        materialTransactionRepository.deleteById(transactionId);
    }

    @Override
    public List<MaterialTransactionDTO> getAllTransactions() {
        List<MaterialTransactionRegister> transactions = materialTransactionRepository.findAll();
        return transactions.stream()
                .map(MaterialTransactionDTO::new)  // DTO로 변환
                .collect(Collectors.toList());
    }


    // 재료명으로 출납 내역 검색
    @Override
    public List<MaterialTransactionDTO> searchByMaterialName(String materialName) {
        List<MaterialTransactionRegister> transactions = materialTransactionRepository.findByMaterialRegisterMaterialNameContaining(materialName);

        // 엔티티를 DTO로 변환
        return transactions.stream()
                .map(transaction -> new MaterialTransactionDTO(transaction))  // DTO로 변환
                .collect(Collectors.toList());
    }


    // 재료코드로 출납 내역 검색
    @Override
    public List<MaterialTransactionDTO> searchByMaterialCode(String materialCode) {
        List<MaterialTransactionRegister> transactions = materialTransactionRepository.findByMaterialRegisterMaterialCodeContaining(materialCode);

        // 엔티티를 DTO로 변환
        return transactions.stream()
                .map(transaction -> new MaterialTransactionDTO(transaction))  // DTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialTransactionDTO> searchTransactions(LocalDate transactionStartDate, LocalDate transactionEndDate, String materialName, String materialCode) {
        // 기본 검색 조건에 따라 쿼리를 생성
        log.info("------------> {}, {}", materialCode, materialName);
        Optional<List<MaterialTransactionRegister>> optionalTransactions = materialTransactionRepository.findSearch(
                transactionStartDate, transactionEndDate, materialName, materialCode);

        // Optional에서 값을 꺼내 처리
        return optionalTransactions.orElse(Collections.emptyList())
                .stream()
                .map(MaterialTransactionDTO::new)  // 결과를 DTO로 변환
                .collect(Collectors.toList());
    }



}


