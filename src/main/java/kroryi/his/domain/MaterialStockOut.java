package kroryi.his.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "material_stock_out")
public class MaterialStockOut {
    // material 테이블과 join. 무한참조 방지
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "material_code", referencedColumnName = "material_Code", nullable = false)
    private MaterialRegister materialRegister;


    // 출고 아이디 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_out_id")
    private Long stockOutId;


    // 출고 일자
    @Column(name = "stock_out_date", nullable = false)
    private LocalDate stockOutDate;

    // 출고량
    @Column(name = "stock_out", nullable = false)
    private Long stockOut;
}
