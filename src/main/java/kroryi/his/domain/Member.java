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
    private boolean retirement;
    private String social;

    @OneToMany(mappedBy = "member", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
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

//    public void addRole(MemberRole role) {
//        this.roleSet.add(role);
//    }

    public void clearRoles() {
        this.roleSet.clear();
    }
}
