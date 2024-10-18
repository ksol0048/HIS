package kroryi.his.service;

import kroryi.his.domain.Member;
import kroryi.his.dto.MemberSecurityDTO;
import kroryi.his.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;


@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}" , username);
        Optional<Member> result = memberRepository.getWithRoles(username);
        if (result.isEmpty()) {
            throw new UsernameNotFoundException(username+"를 찾을수 없습니다.");
        }

        Member member = result.get();

        MemberSecurityDTO memberSecurityDTO =
                new MemberSecurityDTO(
                        member.getMid(),
                        member.getPassword(),
                        member.getEmail(),
                        false,
                        member.getSocial(),
                        member.getRoleSet()
                                .stream().map(memberRole -> new SimpleGrantedAuthority(
                                        "ROLE_" + memberRole.getRoleSet()))
                                .collect(Collectors.toList())
                );

        log.info("----------------memberSecurityDTO: {}", memberSecurityDTO);

        return memberSecurityDTO;
    }
}
