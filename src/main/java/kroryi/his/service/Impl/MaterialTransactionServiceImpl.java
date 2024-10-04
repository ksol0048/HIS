package kroryi.his.service.Impl;

import jakarta.persistence.EntityNotFoundException;
import kroryi.his.domain.CompanyRegister;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import kroryi.his.dto.MaterialDTO;
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
    public MaterialTransactionRegister register(MaterialTransactionDTO materialTransactionDTO) {
        // MaterialDTO를 MaterialRegister로 변환
        MaterialRegister materialRegister = materialRepository.findByMaterialCode(materialTransactionDTO.getMaterialCode())
                .orElseThrow(() -> new EntityNotFoundException("재료를 찾을 수 없습니다: " + materialTransactionDTO.getMaterialCode()));

        MaterialTransactionRegister materialTransactionRegister = modelMapper.map(materialTransactionDTO, MaterialTransactionRegister.class);

        // 입고량과 출고량 기본값 설정
        materialTransactionRegister.setStockIn(materialTransactionDTO.getStockIn() != null ? materialTransactionDTO.getStockIn() : 0L);
        materialTransactionRegister.setStockOut(materialTransactionDTO.getStockOut() != null ? materialTransactionDTO.getStockOut() : 0L);

        // 나머지 필드 설정
        materialTransactionRegister.setTransactionDate(materialTransactionDTO.getTransactionDate());

        // MaterialRegister 객체를 Transaction 엔티티에 설정
        materialTransactionRegister.setMaterialRegister(materialRegister);

        // 재료 출납 정보 저장
        return materialTransactionRepository.save(materialTransactionRegister);
    }

    // 모든 출납 내역 조회
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





}


