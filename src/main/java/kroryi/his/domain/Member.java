package kroryi.his.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "roleSet")
public class Member extends BaseEntity{

    @Id
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

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference  // 부모 엔티티에서 참조 관리
    private Set<MemberRoleSet> roleSet = new HashSet<>();;

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }

    public void changeSocial(String newSocial) {
        this.social = newSocial;
    }

    public void changeRetirement(boolean retirement) {
        this.retirement = retirement;
    }

    public void addRole(MemberRoleSet role) {
        this.roleSet.add(role);
    }

    public void clearRoles() {
        this.roleSet.clear();
    }
}
