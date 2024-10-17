package kroryi.his.repository;

import kroryi.his.domain.Member;
import kroryi.his.domain.MemberRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    @EntityGraph(attributePaths = "roleSet")
    @Query("select m from Member m where m.mid= :mid")
    Optional<Member> getWithRoles(String mid);

    List<Member> findByRoleSet(MemberRole role);

//    List<Member> findUsersByConditions(String userId, String userName, String userRole, String startDate);

    Member findByName(String name);
    Member findByEmail(String email);
    Member findByNameAndPassword(String username, String password);
    Member findByEmailAndPassword(String email, String password);
}
