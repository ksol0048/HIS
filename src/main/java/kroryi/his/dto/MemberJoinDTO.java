package kroryi.his.dto;

import kroryi.his.domain.MemberRole;
import kroryi.his.domain.MemberRoleSet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class MemberJoinDTO {
    private String mid;
    private String name;
    private String password;
    private String email;
    private boolean retirement;
    private String social;

    private Set<MemberRoleSet> roles;  // roleSet 값

}
