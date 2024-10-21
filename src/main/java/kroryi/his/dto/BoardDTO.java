package kroryi.his.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link kroryi.his.domain.Board}
 */
// @Value 어노테이션을 사용하면 필드이 private가 않되는 현상 때문에 규칙 위반
// Config설정에서   .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class BoardDTO implements Serializable {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifyDate;
    private Long bno;
    @NotEmpty
    private String title;
    @NotEmpty
    private String content;
    @NotEmpty
    private String writer;
    private String address;

    private List<String> fileNames;
}