package kroryi.his.dto;

import kroryi.his.domain.MaterialRegister;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialDTO {

    //재료명
    private String materialName;

    //재료코드
    private String materialCode;

    //단위
    private String materialUnit;

    //단가
    private Long materialUnitPrice;

    //최소보관수량
    private Long minQuantity;

    //재고관리여부
    private boolean stockManagementItem;

    //최초등록일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstRegisterDate;

    //업체코드
    private String companyCode;

    //업체명
    private String companyName;

    public MaterialDTO(MaterialRegister materialRegister) {
        this.materialCode = materialRegister.getMaterialCode();
        this.materialName = materialRegister.getMaterialName();
        this.materialUnit = materialRegister.getMaterialUnit();
        this.materialUnitPrice = materialRegister.getMaterialUnitPrice();
        this.minQuantity = materialRegister.getMinQuantity();
        this.stockManagementItem = materialRegister.isStockManagementItem();
        this.firstRegisterDate = materialRegister.getFirstRegisterDate();
        this.companyName = materialRegister.getCompanyRegister().getCompanyName();
        this.companyCode = materialRegister.getCompanyRegister().getCompanyCode();
    }
}
