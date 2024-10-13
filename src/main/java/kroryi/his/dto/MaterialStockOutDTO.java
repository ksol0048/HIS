package kroryi.his.dto;

import kroryi.his.domain.MaterialStockOut;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterialStockOutDTO {

    // 출고 일자 아이디
    private Long stockOutId;

    // 출고 일자
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate stockOutDate;

    // 출고량
    private Long stockOut;


}
