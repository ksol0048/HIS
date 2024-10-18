package kroryi.his.service.Impl;

import kroryi.his.domain.CompanyRegister;
import kroryi.his.dto.CompanyDTO;
import kroryi.his.repository.CompanyRegisterRepository;
import kroryi.his.service.CompanyRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class CompanyRegisterServiceImpl implements CompanyRegisterService {

    private final CompanyRegisterRepository companyRegisterRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<CompanyRegister> getAllCompanies() {
        return companyRegisterRepository.findAll();
    }

    // 업체코드 중복체크 로직
    @Override
    public boolean isCompanyCodeDuplicate(String companyCode) {
        return companyRegisterRepository.existsByCompanyCode(companyCode);
    }

    // 실제 업체 등록시 업체코드 중복체크
    @Override
    public CompanyRegister register(CompanyDTO companyDTO) {
        if (isCompanyCodeDuplicate(companyDTO.getCompanyCode())) {
            throw new IllegalArgumentException("이미 등록된 업체입니다.");
        }

        CompanyRegister companyRegister = modelMapper.map(companyDTO, CompanyRegister.class);
        return companyRegisterRepository.save(companyRegister);
    }

    // 업체 정보 수정 로직
    @Override
    public CompanyRegister update(CompanyDTO companyDTO) {
        // 업체가 존재하는지 확인
        CompanyRegister existingCompany = companyRegisterRepository.findById(companyDTO.getCompanyCode())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 업체 코드입니다: " + companyDTO.getCompanyCode()));

        // 기존 업체 정보를 새로운 값으로 업데이트
        existingCompany.setCompanyName(companyDTO.getCompanyName());
        existingCompany.setBusinessNumber(companyDTO.getBusinessNumber());
        existingCompany.setCompanyNumber(companyDTO.getCompanyNumber());
        existingCompany.setManagerName(companyDTO.getManagerName());
        existingCompany.setManagerNumber(companyDTO.getManagerNumber());
        existingCompany.setCompanyMemo(companyDTO.getCompanyMemo());

        return companyRegisterRepository.save(existingCompany);  // 업데이트된 업체 정보 저장
    }

    // 업체 검색
    @Override
    public List<CompanyRegister> searchByName(String companyName) {
        return companyRegisterRepository.findByCompanyNameContainingIgnoreCase(companyName);
    }

    // 업체 삭제
    @Override
    public void deleteCompany(String companyCode) {
        companyRegisterRepository.deleteById(companyCode);
    }






}