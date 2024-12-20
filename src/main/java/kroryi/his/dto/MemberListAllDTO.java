package kroryi.his.dto;

import kroryi.his.domain.MemberRole;
import kroryi.his.domain.MemberRoleSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberListAllDTO {
    private String mid;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String tel;
    private LocalDateTime regDate;
    private Set<MemberRole> roles;
}
