package kroryi.his.dto;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import lombok.*;

import java.time.LocalDate;

@Builder
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialTransactionDTO {

    //입출아이디
    private Long transactionId;

    //입출일자
    private LocalDate transactionDate;

    //입고량
    private Long stockIn;

    //출고량
    private Long stockOut;

    //잔량
    private Long remainingStock;

    //안전재고량미달품목
    private boolean belowSafetyStock;

    //재료명
    private String materialName;

    //재료코드
    private String materialCode;

    //업체코드
    private String companyCode;

    //업체명
    private String companyName;

    //담당자연락처
    private String managerNumber;

    public MaterialTransactionDTO(MaterialTransactionRegister materialTransactionRegister) {
        this.transactionId = materialTransactionRegister.getTransactionId();
        this.transactionDate = materialTransactionRegister.getTransactionDate();
        this.stockIn = materialTransactionRegister.getStockIn();
        this.stockOut = materialTransactionRegister.getStockOut();
        this.remainingStock = materialTransactionRegister.getRemainingStock();
        this.belowSafetyStock = materialTransactionRegister.isBelowSafetyStock();

        // MaterialRegister가 null이 아닌 경우에만 데이터 설정
        if (materialTransactionRegister.getMaterialRegister() != null) {
            this.materialCode = materialTransactionRegister.getMaterialRegister().getMaterialCode();
            this.materialName = materialTransactionRegister.getMaterialRegister().getMaterialName();

            // CompanyRegister가 null이 아닌 경우에만 데이터 설정
            if (materialTransactionRegister.getMaterialRegister().getCompanyRegister() != null) {
                this.companyName = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getCompanyName();
                this.companyCode = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getCompanyCode();
                this.managerNumber = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getManagerNumber();
            }
        }
    }



}
