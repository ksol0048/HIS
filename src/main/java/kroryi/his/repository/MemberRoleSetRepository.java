package kroryi.his.repository;

import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRoleSet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@EnableJpaRepositories
public interface MemberRoleSetRepository extends JpaRepository<MemberRoleSet, Long> {

    @Modifying
    @Query("DELETE FROM MemberRoleSet m where m.member.mid= :mid")
    void deleteWithRolesByMemberId(@Param("mid") String mid);
}
