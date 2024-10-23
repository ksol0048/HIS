package kroryi.his.service;

import jakarta.transaction.Transactional;
import kroryi.his.repository.MemberRoleSetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Log4j2
@Service
public class MemberRoleSetService {

    private final  MemberRoleSetRepository memberRoleSetRepository;

    @Transactional  // 트랜잭션 적용
    public void deleteRolesByMemberId(String mid) {
        memberRoleSetRepository.deleteWithRolesByMemberId(mid);
    }

}