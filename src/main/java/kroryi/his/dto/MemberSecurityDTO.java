package kroryi.his.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class MemberSecurityDTO extends User {
   /* public MemberSecurityDTO(String username,
                             String password,
                             String email,
                             boolean retirement,
                             String social,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);


    }*/
   private final String name;
    private final boolean retirement;
    private final String social;

    public MemberSecurityDTO(MemberJoinDTO memberJoinDTO, Collection<? extends GrantedAuthority> authorities) {
        super(memberJoinDTO.getMid(), memberJoinDTO.getPassword(), authorities);
        this.name = memberJoinDTO.getName();
        this.retirement = memberJoinDTO.isRetirement();
        this.social = memberJoinDTO.getSocial();
    }

    public String getName() {
        return name;
    }

    public boolean isRetirement() {
        return retirement;
    }

    public String getSocial() {
        return social;
    }

    @Override
    public String toString() {
        return "MemberSecurityDTO{" +
                "username='" + getUsername() + '\'' +
                ", name='" + name + '\'' +
                ", retirement=" + retirement +
                ", social='" + social + '\'' +
                ", authorities=" + getAuthorities() +
                '}';
    }

}