package kroryi.his.service;

import kroryi.his.domain.CompanyRegister;
import kroryi.his.dto.CompanyDTO;

import java.util.List;


public interface CompanyRegisterService {
    //가짜데이터 등록 확인용
    //CompanyRegister registerCompany();

    List<CompanyRegister> getAllCompanies();

    CompanyRegister register(CompanyDTO companyDTO);

    boolean isCompanyCodeDuplicate(String companyCode);

    List<CompanyRegister> searchByName(String companyName);

    void deleteCompany(String companyCode);

    CompanyRegister update(CompanyDTO companyDTO);

}
