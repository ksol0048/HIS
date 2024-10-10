package kroryi.his.service.Impl;

import kroryi.his.domain.CompanyRegister;
import kroryi.his.domain.MaterialRegister;
import kroryi.his.dto.MaterialDTO;
import kroryi.his.repository.CompanyRegisterRepository;
import kroryi.his.repository.MaterialRegisterRepository;
import kroryi.his.service.MaterialRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Log4j2
@Service
public class MaterialRegisterServiceImpl implements MaterialRegisterService {

    private final MaterialRegisterRepository materialRepository;
    private final CompanyRegisterRepository companyRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<MaterialRegister> getAllMaterial() {
        return materialRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(MaterialRegister::getFirstRegisterDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialDTO> getAllMaterialsWithCompany() {
        List<MaterialRegister> materials = materialRepository.findAll();
        return materials.stream()
                .sorted(Comparator.comparing(MaterialRegister::getFirstRegisterDate).reversed())
                .map(material -> new MaterialDTO(material)) // MaterialDTO로 변환
                .collect(Collectors.toList());
    }

    @Override
    public MaterialRegister register(MaterialDTO materialDTO) {
        // CompanyRegister 조회
        CompanyRegister companyRegister = companyRepository.findOptionalByCompanyCode(materialDTO.getCompanyCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 회사 정보를 찾을 수 없습니다: " + materialDTO.getCompanyCode()));

        // MaterialDTO를 MaterialRegister로 변환
        MaterialRegister materialRegister = modelMapper.map(materialDTO, MaterialRegister.class);

        // CompanyRegister 설정
        materialRegister.setCompanyRegister(companyRegister);

        // 재료 저장
        return materialRepository.save(materialRegister);
    }


    // 재료 삭제
    @Override
    @Transactional
    public void customDeleteByMaterialCode(String materialCode) {
        if (!materialRepository.existsByMaterialCode(materialCode)) {
            throw new IllegalArgumentException("존재하지 않는 재료 코드입니다: " + materialCode);
        }
        materialRepository.customDeleteByMaterialCode(materialCode);
    }


    // 업체명으로 검색
    public List<MaterialRegister> searchByCompanyName(String companyName) {
        return materialRepository.findByCompanyRegisterCompanyNameContaining(companyName)
                .stream()
                .sorted(Comparator.comparing(MaterialRegister::getFirstRegisterDate).reversed())
                .collect(Collectors.toList());
    }

    // 재료명으로 검색
    public List<MaterialRegister> searchByMaterialName(String materialName) {
        return materialRepository.findByMaterialNameContaining(materialName)
                .stream()
                .sorted(Comparator.comparing(MaterialRegister::getFirstRegisterDate).reversed())
                .collect(Collectors.toList());
    }

    // 업체명과 재료명으로 검색
    public List<MaterialRegister> searchByCompanyNameAndMaterialName(String companyName, String materialName) {
        return materialRepository.findByCompanyRegisterCompanyNameContainingAndMaterialNameContaining(companyName, materialName)
                .stream()
                .sorted(Comparator.comparing(MaterialRegister::getFirstRegisterDate).reversed())
                .collect(Collectors.toList());
    }

    public boolean isNewMaterial(String materialCode) {
        return !materialRepository.existsByMaterialCode(materialCode);
    }


    @Override
    public void updateMaterial(MaterialDTO materialDTO) {
        Optional<MaterialRegister> optionalMaterial = materialRepository.findByMaterialCode(materialDTO.getMaterialCode());

        MaterialRegister materialRegister;
        boolean isNewMaterial = false;  // 새로운 재료 여부

        if (optionalMaterial.isPresent()) {
            // 기존 재료가 있을 경우 수정
            materialRegister = optionalMaterial.get();
            log.info("기존 재료를 수정합니다: {}", materialRegister);
        } else {
            // 새로운 재료를 등록
            log.info("재료가 없으므로 새로 등록합니다: {}", materialDTO.getMaterialCode());
            materialRegister = new MaterialRegister();  // 새로운 객체 생성
            materialRegister.setMaterialCode(materialDTO.getMaterialCode());  // 새 재료의 코드 설정
            isNewMaterial = true;  // 새로운 재료 등록
        }

        // 공통적으로 재료 정보 수정/등록
        materialRegister.setMaterialName(materialDTO.getMaterialName());
        materialRegister.setMaterialUnit(materialDTO.getMaterialUnit());
        materialRegister.setMaterialUnitPrice(materialDTO.getMaterialUnitPrice());
        materialRegister.setMinQuantity(materialDTO.getMinQuantity());
        materialRegister.setStockManagementItem(materialDTO.isStockManagementItem());

        CompanyRegister company = companyRepository.findOptionalByCompanyCode(materialDTO.getCompanyCode())
                .orElseThrow(() -> new RuntimeException("해당 회사 정보를 찾을 수 없습니다: " + materialDTO.getCompanyCode()));
        materialRegister.setCompanyRegister(company);

        // 최종적으로 재료 정보 저장
        materialRepository.save(materialRegister);

        // 새로 등록되었는지, 수정되었는지에 따라 다른 메시지를 반환
        if (isNewMaterial) {
            log.info("새로운 재료가 등록되었습니다: {}", materialDTO.getMaterialCode());
        } else {
            log.info("재료가 수정되었습니다: {}", materialDTO.getMaterialCode());
        }


    }

}

