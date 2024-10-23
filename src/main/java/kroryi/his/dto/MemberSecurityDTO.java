package kroryi.his.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

public class MemberSecurityDTO extends User {

    private final String name;
    private final String password;


    public MemberSecurityDTO(MemberJoinDTO memberJoinDTO, Collection<? extends GrantedAuthority> authorities) {

        super(memberJoinDTO.getMid(), memberJoinDTO.getPassword(), authorities);

        this.name = memberJoinDTO.getName();
        this.password = memberJoinDTO.getPassword();
    }
}