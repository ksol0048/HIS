package kroryi.his.repository;

import kroryi.his.domain.MemberRoleSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
public interface MemberRoleSetRepository extends JpaRepository<MemberRoleSet, String> {
}
