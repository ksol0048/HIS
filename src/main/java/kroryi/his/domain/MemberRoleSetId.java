package kroryi.his.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class MemberRoleSetId implements Serializable {

    // Getters and Setters (여기서 setter의 타입도 Long인지 확인)
    private String member;
    private MemberRole roleSet;

    // 기본 생성자
    public MemberRoleSetId() {}

    // equals와 hashCode 메서드 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberRoleSetId that = (MemberRoleSetId) o;
        return Objects.equals(member, that.member) && roleSet == that.roleSet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, roleSet);
    }

}