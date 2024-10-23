package kroryi.his.dto;

import kroryi.his.domain.MemberRole;
import kroryi.his.domain.MemberRoleSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberJoinDTO {
    private String mid;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String tel;
    private boolean retirement;
    private String social;
    private int zipCode;
    private String address;
    private String detailAddress;
    private String note;

    private Set<MemberRoleSet> roles;  // roleSet 값

    public MemberJoinDTO(String mid, String email, String name, String password, boolean retirement, String social, LocalDateTime regDate, LocalDateTime modDate, String address, String detailAddress, int zipCode) {
    }
}
