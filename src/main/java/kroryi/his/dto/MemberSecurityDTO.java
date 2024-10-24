package kroryi.his.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class MemberSecurityDTO extends User {

    private final String name;
    private final String password;


    public MemberSecurityDTO(MemberJoinDTO memberJoinDTO, Collection<? extends GrantedAuthority> authorities) {

        super(memberJoinDTO.getMid(), memberJoinDTO.getPassword(), authorities);
        System.out.println("MemberSecurityDTO 00>"+memberJoinDTO);

        this.name = memberJoinDTO.getName();
        this.password = memberJoinDTO.getPassword();
    }

    @Override
    public String toString() {
        return "MemberSecurityDTO{" +
                "username='" + getUsername() + '\'' +
                ", name='" + name + '\'' +
                ", authorities=" + getAuthorities() +
                '}';
    }

}