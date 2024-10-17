package kroryi.his.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberRoleSetDTO {
    private String role;  // JSON에서 전달되는 role 값을 받음

    // 역할을 설정하는 생성자
    public MemberRoleSetDTO(String role) {
        this.role = role;
    }
}