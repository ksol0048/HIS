package kroryi.his.dto;

import kroryi.his.domain.MaterialRegister;
import kroryi.his.domain.MaterialTransactionRegister;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

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

    //입고일자
    private LocalDate stockInDate;

    //입고량
    private Long stockIn;

    //단위
    private String materialUnit;

    //현재고량
    private Long remainingStock;

    //최소보관수량
    private Long minQuantity;

    //재고관리품목 여부
    private Boolean stockManagementItem;

    //재료명
    private String materialName;

    //재료코드
    private String materialCode;

    //최초등록일
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstRegisterDate;

    //업체코드
    private String companyCode;

    //업체명
    private String companyName;

    //담당자연락처
    private String managerNumber;


    // 안전 재고 여부
    private boolean belowSafetyStock;


    // 안전재고량 미달품목 여부 (실시간 계산 함수)
    public boolean isBelowSafetyStock() {
        return this.remainingStock != null && this.minQuantity != null && this.remainingStock < this.minQuantity;
    }

    // 빨간색 그리드를 위한 필드
    private boolean isHighlighted;


    public MaterialTransactionDTO(MaterialTransactionRegister materialTransactionRegister) {
        this.transactionId = materialTransactionRegister.getTransactionId();
        this.stockInDate = materialTransactionRegister.getStockInDate();
        this.stockIn = materialTransactionRegister.getStockIn();
        this.remainingStock = materialTransactionRegister.getRemainingStock();
        this.firstRegisterDate = materialTransactionRegister.getMaterialRegister().getFirstRegisterDate();
        this.isHighlighted = false; // 초기값


        // MaterialRegister가 null이 아닌 경우에만 데이터 설정
        if (materialTransactionRegister.getMaterialRegister() != null) {
            this.materialCode = materialTransactionRegister.getMaterialRegister().getMaterialCode();
            this.materialName = materialTransactionRegister.getMaterialRegister().getMaterialName();
            this.stockManagementItem = materialTransactionRegister.getMaterialRegister().isStockManagementItem();
            this.materialUnit = materialTransactionRegister.getMaterialRegister().getMaterialUnit();
            this.minQuantity = materialTransactionRegister.getMaterialRegister().getMinQuantity();

            // CompanyRegister가 null이 아닌 경우에만 데이터 설정
            if (materialTransactionRegister.getMaterialRegister().getCompanyRegister() != null) {
                this.companyName = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getCompanyName();
                this.companyCode = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getCompanyCode();
                this.managerNumber = materialTransactionRegister.getMaterialRegister().getCompanyRegister().getManagerNumber();
            } else {
                this.companyName = "N/A";  // Company 정보가 없을 경우 기본값 설정
                this.managerNumber = "N/A";  // Manager 정보가 없을 경우 기본값 설정
            }
        } else {
            this.materialCode = "N/A";  // Material 정보가 없을 경우 기본값 설정
            this.materialName = "N/A";
        }
    }
}