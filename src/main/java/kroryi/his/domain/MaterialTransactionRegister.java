package kroryi.his.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "material_transactions")
public class MaterialTransactionRegister {

    //material 테이블과 join. 무한참조 방지
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "material_Code", nullable = false)
    private MaterialRegister materialRegister;


    //입출일자 아이디(PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_Id")
    private Long transactionId;

    //입고일자
    @Column(name = "stock_in_date", nullable = false)
    private LocalDate stockInDate;

    //입고량
    @Column(name = "stock_In")
    private Long stockIn;

    //잔량
    @Column(name = "remaining_Stock")
    private Long remainingStock;


    //안전재고량 미달품목
    @Column(name = "below_Safety_Stock", nullable = false)
    private boolean belowSafetyStock;




}
